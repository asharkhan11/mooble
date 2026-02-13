//package in.ashar.mooble.security;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
//import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
//import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
//import org.springframework.security.oauth2.core.oidc.user.OidcUser;
//import org.springframework.stereotype.Service;
//
//import java.util.Set;
//@Service
//@Slf4j
//public class CustomOidcUserService extends OidcUserService {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Override
//    public OidcUser loadUser(OidcUserRequest userRequest) {
//        OidcUser oidcUser = super.loadUser(userRequest);
//
//        String email = oidcUser.getEmail();
//        log.info("Looking for user: {}", email);
//
//
//        var user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new BadCredentialsException("User not registered in system"));
//
//        var authorities = Set.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName()));
//
//        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
//    }
//}
//
//
