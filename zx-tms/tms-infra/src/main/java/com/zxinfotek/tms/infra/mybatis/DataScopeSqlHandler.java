package com.zxinfotek.tms.infra.mybatis;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.zxinfotek.tms.infra.context.RequestContext;
import com.zxinfotek.tms.infra.context.RequestContextHolder;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据范围 SQL 处理器：对标注 @DataScope 的 Mapper 方法追加租户条件与机构范围条件。
 *
 * <p>租户条件对平台账户（tenant_id = 0）跳过，机构范围按本次请求命中的权限码对应的数据范围计算，
 * 取值规则见详细设计 7.1 与 7.3。</p>
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
public class DataScopeSqlHandler implements MultiDataPermissionHandler {

    private static final Logger log = LoggerFactory.getLogger(DataScopeSqlHandler.class);

    private static final String COUNT_SUFFIX = "_mpCount";

    private final Map<String, Optional<DataScope>> annotationCache = new ConcurrentHashMap<>();

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        Optional<DataScope> annotation = resolveAnnotation(mappedStatementId);
        if (annotation.isEmpty()) {
            return null;
        }
        RequestContext context = RequestContextHolder.peek();
        if (context == null || !context.isAuthenticated()) {
            return null;
        }
        DataScope config = annotation.get();
        String alias = table.getAlias() != null ? table.getAlias().getName() : table.getName();

        Expression condition = null;
        if (!config.tenantColumn().isEmpty() && !context.isPlatform() && context.getTenantId() != null) {
            EqualsTo tenantEquals = new EqualsTo();
            tenantEquals.setLeftExpression(new Column(alias + "." + config.tenantColumn()));
            tenantEquals.setRightExpression(new LongValue(context.getTenantId()));
            condition = tenantEquals;
        }

        Expression orgCondition = buildOrgCondition(context, config, alias);
        if (orgCondition != null) {
            condition = condition == null ? orgCondition : new AndExpression(condition, orgCondition);
        }
        return condition;
    }

    private Expression buildOrgCondition(RequestContext context, DataScope config, String alias) {
        com.zxinfotek.tms.common.enums.DataScope scope = context.getDataScope();
        if (scope == null) {
            return null;
        }
        if (scope == com.zxinfotek.tms.common.enums.DataScope.SELF_ORG) {
            if (config.orgIdColumn().isEmpty() || context.getOrgId() == null) {
                return null;
            }
            EqualsTo equals = new EqualsTo();
            equals.setLeftExpression(new Column(alias + "." + config.orgIdColumn()));
            equals.setRightExpression(new LongValue(context.getOrgId()));
            return equals;
        }
        if (config.orgPathColumn().isEmpty() || context.getOrgPath() == null) {
            return null;
        }
        LikeExpression like = new LikeExpression();
        like.setLeftExpression(new Column(alias + "." + config.orgPathColumn()));
        like.setRightExpression(new StringValue(context.getOrgPath() + "%"));
        return like;
    }

    private Optional<DataScope> resolveAnnotation(String mappedStatementId) {
        return annotationCache.computeIfAbsent(mappedStatementId, id -> {
            String statementId = id.endsWith(COUNT_SUFFIX)
                    ? id.substring(0, id.length() - COUNT_SUFFIX.length()) : id;
            int split = statementId.lastIndexOf('.');
            if (split <= 0) {
                return Optional.empty();
            }
            String className = statementId.substring(0, split);
            String methodName = statementId.substring(split + 1);
            try {
                Class<?> mapperClass = Class.forName(className);
                for (Method method : mapperClass.getMethods()) {
                    if (method.getName().equals(methodName) && method.isAnnotationPresent(DataScope.class)) {
                        return Optional.of(method.getAnnotation(DataScope.class));
                    }
                }
            } catch (ClassNotFoundException e) {
                log.debug("数据范围拦截未找到 Mapper 类 {}", className);
            }
            return Optional.empty();
        });
    }
}
