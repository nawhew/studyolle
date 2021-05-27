package studyolle.account.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import studyolle.account.application.AccountService;
import studyolle.account.domain.Account;
import studyolle.account.dto.SignUpForm;

import javax.validation.Valid;

@Controller
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
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
        return "redirect:/";
    }
}
