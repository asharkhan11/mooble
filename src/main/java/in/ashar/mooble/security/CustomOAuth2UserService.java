//package in.ashar.mooble.security;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
//import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
//import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Service;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//
//import java.util.Set;
//
//@Service
//@Slf4j
//public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest)
//            throws OAuth2AuthenticationException {
//
//        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
//        String email = oAuth2User.getAttribute("email");
//
//        log.info("Looking for user: {}", email);
//        var user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new OAuth2AuthenticationException("User not registered in system"));
//
//        String role = user.getRole().getRoleName(); // e.g. "ADMIN"
//        Set<GrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority("ROLE_" + role));
//
//        return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), "email");
//    }
//}