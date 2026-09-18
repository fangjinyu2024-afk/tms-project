package com.zxinfotek.tms.core.iam.api;

import com.zxinfotek.tms.common.enums.SessionInvalidReason;
import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.core.iam.api.model.ForceLogoutRequest;
import com.zxinfotek.tms.core.iam.api.model.LoginTokenDTO;
import com.zxinfotek.tms.core.iam.api.model.SessionContextDTO;
import com.zxinfotek.tms.core.iam.api.model.SessionQuery;
import com.zxinfotek.tms.core.iam.api.model.SessionVO;

public interface LoginSessionService {

    /** 建立登录会话，返回明文令牌与滑动有效期；令牌只在本次返回。 */
    LoginTokenDTO createSession(SessionContextDTO context);

    /** 按令牌还原会话上下文，令牌不存在或已失效返回 null。 */
    SessionContextDTO authenticate(String token);

    /** 刷新滑动有效期与最近活动时间，超过绝对有效期返回 false。 */
    boolean refresh(SessionContextDTO context);

    void updateSessionPermissions(SessionContextDTO context);

    void invalidateCurrent(SessionInvalidReason reason);

    void invalidateByMember(Long memberId, SessionInvalidReason reason);

    void invalidateByOrgPath(Long tenantId, String orgPath, SessionInvalidReason reason);

    void invalidateByTenant(Long tenantId, SessionInvalidReason reason);

    PageResult<SessionVO> page(SessionQuery query);

    void forceLogout(Long sessionId, ForceLogoutRequest request);

    String export(SessionQuery query);

    /** 清理已过期会话，由定时任务调用。 */
    int cleanExpired();
}
