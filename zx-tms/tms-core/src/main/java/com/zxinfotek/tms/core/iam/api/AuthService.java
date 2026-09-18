package com.zxinfotek.tms.core.iam.api;

import com.zxinfotek.tms.core.iam.api.model.ChangePasswordRequest;
import com.zxinfotek.tms.core.iam.api.model.EmailVerifyRequest;
import com.zxinfotek.tms.core.iam.api.model.ForgotPasswordRequest;
import com.zxinfotek.tms.core.iam.api.model.LoginRequest;
import com.zxinfotek.tms.core.iam.api.model.LoginVO;
import com.zxinfotek.tms.core.iam.api.model.ProfileVO;
import com.zxinfotek.tms.core.iam.api.model.ResetPasswordRequest;
import com.zxinfotek.tms.core.iam.api.model.UpdateProfileRequest;

public interface AuthService {

    LoginVO login(LoginRequest request);

    void logout();

    ProfileVO currentProfile();

    void updateProfile(UpdateProfileRequest request);

    void changePassword(ChangePasswordRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPasswordByToken(ResetPasswordRequest request);

    void emailVerify(EmailVerifyRequest request);
}
