package studyolle.account.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import studyolle.account.application.AccountService;
import studyolle.account.domain.Account;
import studyolle.account.domain.security.CurrentUserAccount;
import studyolle.account.dto.SignUpForm;
import studyolle.account.dto.SignUpFormValidator;

import javax.validation.Valid;

@Controller
public class AccountController {

    private final AccountService accountService;
    private final SignUpFormValidator signUpFormValidator;

    public AccountController(AccountService accountService, SignUpFormValidator signUpFormValidator) {
        this.accountService = accountService;
        this.signUpFormValidator = signUpFormValidator;
    }

    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(this.signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute("signUpForm", new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUp(@ModelAttribute @Valid SignUpForm signUpForm, Errors errors) {
        if(errors.hasErrors()) {
            return "account/sign-up";
        }

        Account account = this.accountService.save(signUpForm);
        this.accountService.sendSignUpCheckEmail(account);
        this.accountService.login(account);
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(@RequestParam String token, @RequestParam String email, Model model) {
        Account account = this.accountService.checkEmailToken(token, email);
        String view = "account/checked-email";
        if(account != null) {
            model.addAttribute("nickname", account.getNickname());
            this.accountService.login(account);
            return view;
        }
        model.addAttribute("error", "fail.checked.email-token");
        return view;
    }

    @GetMapping("/check-email")
    public String checkEmail(@CurrentUserAccount Account account, Model model) {
        model.addAttribute("email", account.getEmail());
        return "account/check-email";
    }


    @GetMapping("/resend-confirm-email")
    public String resendConfirmEmail(@CurrentUserAccount Account account, Model model) {
        if(!account.canSendCheckEmail()) {
            model.addAttribute("error", "?????? ???????????? 1????????? ????????? ????????? ??? ????????????.");
            model.addAttribute("email", account.getEmail());
            model.addAttribute("lastSendEmailTime", account.getEmailCheckTokenGeneratedAt());
            return "account/check-email";
        }

        this.accountService.sendSignUpCheckEmail(account);
        return "redirect:/";
    }

    @GetMapping("/profile/{nickname}")
    public String profile(@PathVariable String nickname, Model model, @CurrentUserAccount Account account) {
        Account accountByNickname = this.accountService.findByNickname(nickname);

        model.addAttribute("account", accountByNickname);
        model.addAttribute("isOwner", accountByNickname.equals(account));
        return "account/profile";
    }
}
