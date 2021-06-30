package studyolle.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import studyolle.account.domain.Account;
import studyolle.account.domain.security.CurrentUserAccount;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Slf4j
public class ExceptionAdvice {

    @ExceptionHandler
    public String handleRuntimeException(@CurrentUserAccount Account account, HttpServletRequest request
            , RuntimeException e) {
        if (account != null) {
            log.info("'{}' requested '{}'", account.getNickname(), request.getRequestURI());
        } else {
            log.info("requested '{}'", request.getRequestURI());
        }
        log.error("bad request", e);
        return "error";
    }
}
