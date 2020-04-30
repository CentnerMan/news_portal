package ru.geek.news_portal.controllers;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import ru.geek.news_portal.base.entities.User;
import ru.geek.news_portal.base.repo.RoleRepository;
import ru.geek.news_portal.dto.EmailDTO;
import ru.geek.news_portal.dto.NewPasswordDTO;
import ru.geek.news_portal.dto.UpdatePasswordDTO;
import ru.geek.news_portal.dto.UserAccountDTO;
import ru.geek.news_portal.services.EmailService;
import ru.geek.news_portal.services.SecurityService;
import ru.geek.news_portal.services.UserService;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.MalformedURLException;
import java.security.Principal;
import java.util.Objects;
import java.util.UUID;

import static ru.geek.news_portal.utils.HttpRequestUtils.getAppUrl;

/**
 * GeekBrains Java, news_portal.
 *
 * @author Anatoly Lebedev
 * @version 1.0.0 21.03.2020
 * @link https://github.com/Centnerman
 * fix Dmitriy Ostrovskiy
 */

@Controller
public class UserController {
    private final RoleRepository roleRepository;
    private EmailService mailService;
    private final UserService userService;
    private SecurityService securityService;

    @Autowired
    public UserController(RoleRepository roleRepository,
                          UserService userService) {
        this.roleRepository = roleRepository;
        this.userService = userService;
    }

    @Autowired
    public void setMailService(EmailService service) {
        mailService = service;
    }

    @ModelAttribute("emailObj")
    public EmailDTO emailObj() {
        return new EmailDTO();
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    //-------------------------------------------------------------------------------

    @GetMapping({"/user/edituser", "/user/edituser/{username}"})
    public String editUserGet(Model model, @PathVariable(value = "username", required = false) String username,
                              Principal principal, HttpServletRequest request) {
        if (!request.isRequestedSessionIdValid()) {
            return "redirect:/";
        }
        UserAccountDTO userDTO;

        if (username == null || (principal.getName().equals(username) && userService.isUserExist(username))) {
            userDTO = userService.userToDTO(principal.getName());
            model.addAttribute("user", userDTO);
        } else {
            return "redirect:/";
        }
//        model.addAttribute("edit", true);
        return "ui/personal";
    }

    //-------------------------------------------------------------------------------

    @PostMapping("/user/edituser")
    public String editUserPost(Model model, @ModelAttribute("user") @Valid UserAccountDTO userAccountDTO,
                               Principal principal, HttpServletRequest request) {
//        if (!request.isRequestedSessionIdValid()) {
//            return "ui/personal";
//        }
        // todo Переделать с учетом фильтров

        String errorMsg = "";

        if (!userAccountDTO.getUsername().equals(principal.getName())) {
            errorMsg = errorMsg + "Username wrong,";
        }
        if (userAccountDTO.getFirstName().length() < 2 || userAccountDTO.getFirstName() == null) {
            errorMsg = errorMsg + " The first name must be longer than or equal to 1 characters,";
        }
        if (userAccountDTO.getLastName().length() < 2 || userAccountDTO.getLastName() == null) {
            errorMsg = errorMsg + " The last name must be longer than or equal to 1 characters,";
        }
        if (userAccountDTO.getEmail().length() < 6 || userAccountDTO.getEmail() == null) {
            errorMsg = errorMsg + " The email is not correctly";
        }

        if (errorMsg.length() > 0) {
            model.addAttribute("error", errorMsg);
        } else {
            userService.saveDTO(userAccountDTO);
            model.addAttribute("success", "The changing succesful");
        }
        model.addAttribute("user", userService.userToDTO(principal.getName()));
        return "ui/personal";
    }

    //-------------------------------------------------------------------------------

    @GetMapping({"/user/favorite", "/user/favorite/{username}"})
    public String userFavorites(Model model, @PathVariable(value = "username", required = false) String username,
                                HttpServletRequest request) {
        if (!request.isRequestedSessionIdValid()) {
            return "redirect:/";
        }
        model.addAttribute("user", userService.findByUsername(username));
        return "ui/user_favorites";
    }

    //-------------------------------------------------------------------------------

    @GetMapping({"/user/comment", "/user/comment/{username}"})
    public String userComments(Model model, @PathVariable(value = "username", required = false) String username,
                               HttpServletRequest request) {
        if (!request.isRequestedSessionIdValid()) {
            return "redirect:/";
        }
        model.addAttribute("user", userService.findByUsername(username));
        return "ui/user_comments";
    }

    //-------------------------------------------------------------------------------

    @GetMapping({"/user/change_password", "/user/change_password/{username}"})
    public String userChangePassword(Model model, @PathVariable(value = "username", required = false) String username,
                                     HttpServletRequest request) {
        if (!request.isRequestedSessionIdValid()) {
            return "redirect:/";
        }
        return "ui/changepass";
    }

    //-------------------------------------------------------------------------------

    @PostMapping("/user/change_password")
    public String userChangePassword(@ModelAttribute("password") @Valid UpdatePasswordDTO updatePasswordDTO,
                                     BindingResult bindingResult, Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName());

        if (!userService.checkPassword(user, updatePasswordDTO.getOldPassword())) {
            model.addAttribute("error", "Wrong current password");
            return "ui/changepass";
        }

        if (!updatePasswordDTO.getNewPassword().equals(updatePasswordDTO.getMatchingPassword())) {
            model.addAttribute("error", "The password fields must match");
            return "ui/changepass";
        }

        userService.updatePassword(user, updatePasswordDTO.getNewPassword());
        model.addAttribute("success", "The password change successful");
        return "ui/changepass";
    }

    //-------------------------------------------------------------------------------

    @GetMapping({"/user/setting", "/user/setting/{username}"})
    public String userSettings(Model model, @PathVariable(value = "username", required = false) String username,
                               HttpServletRequest request) {
        if (!request.isRequestedSessionIdValid()) {
            return "redirect:/";
        }
        model.addAttribute("user", userService.findByUsername(username));
        return "ui/settings";
    }

    //-------------------------------------------------------------------------------

    @GetMapping("/forgot")
    public String forgotPassword() {
        return "ui/forgot";
    }

    //-------------------------------------------------------------------------------

    @GetMapping("/reset")
    public String resetPassword(Model model, Principal principal) {
        return "ui/reset";
    }

    //-------------------------------------------------------------------------------

    @PostMapping("/reset")
    public String resetPassword(@ModelAttribute("password") NewPasswordDTO newPassword,
                                Principal principal, Model model) {

        User user = userService.findByUsername(principal.getName());

        if (!newPassword.getNewPassword().equals(newPassword.getMatchingPassword())) {
            model.addAttribute("error", "The password fields must match");
            return "ui/reset";
        }

        userService.updatePassword(user, newPassword.getNewPassword());
        model.addAttribute("success", "The password change successful");

        return "ui/reset";
    }

    //-------------------------------------------------------------------------------
    //-------------------------------------------------------------------------------
    //-------------------------------------------------------------------------------

    @GetMapping("/resetPassword")
    public String resetPasswordPage()
    {
        return "/resetPassword";
    }


    @SneakyThrows
    @PostMapping("/resetPassword")
    public String resetPassword(HttpServletRequest req,
                                @ModelAttribute("emailObj") @Valid EmailDTO emailObj,
                                BindingResult bindRes, Model model)
    {
        if (bindRes.hasErrors())
            return "/resetPassword";

        if (!userService.isUserExist(emailObj.username))
        {
            model.addAttribute("error", "Неверное имя пользователя");
            return "/resetPassword";
        }

        sendResetPasswordEmail(req, emailObj);
        model.addAttribute("success", "Письмо для сброса пароля успешно отправлено");
        return "/resetPassword";
    }


    private void sendResetPasswordEmail(HttpServletRequest req, EmailDTO emailObj)
            throws MalformedURLException, MessagingException
    {
        String appUrl = getAppUrl(req).toExternalForm();
        User user = userService.findByUsername(emailObj.username);
        String token = UUID.randomUUID().toString();

        String url = appUrl + "/resetPassword/new?" +
                "id=" + user.getId() + "&token=" + token;

        String html = "<p>Для восстановления пароля перейдите по ссылке</p>" +
                "<a href='" + url + "'>reset password</a>";

        userService.createPasswordResetToken(user, token);
        mailService.sendHTMLmessage(emailObj.email, "Восстановление пароля", html);
    }


    @GetMapping("/resetPassword/new")
    public String newPasswordPage(Model model, @RequestParam("id") long id,
                                  @RequestParam("token") String token)
    {
        try
        {
            securityService.validatePasswordResetToken(id, token);
        }
        catch (SecurityService.PasswordResetTokenException e)
        {
            model.addAttribute("message", e.getMessage());
            return "/login";
        }

        model.addAttribute("newPassword", new NewPasswordDTO());
        return "/newPassword";
    }


    @PostMapping("/resetPassword/save")
    @PreAuthorize("hasAuthority('CHANGE__PASSWORD__PRIVILEGE')")
    public String savePassword(@ModelAttribute("newPassword") @Valid NewPasswordDTO password,
                               BindingResult bindRes, Model model)
    {
        if (bindRes.hasErrors())
            return "/newPassword";

        if (!Objects.equals(password.getNewPassword(), password.getMatchingPassword()))
        {
            model.addAttribute("error", "Повторенный пароль не совпадает с введенным");
            return "/newPassword";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        userService.updatePassword(user, password.getNewPassword());
        model.addAttribute("success", "пароль успешно сохранен");

        return "/newPassword";
    }

}
