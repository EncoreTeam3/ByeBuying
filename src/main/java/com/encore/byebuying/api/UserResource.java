package com.encore.byebuying.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.encore.byebuying.config.properties.AppProperties;
import com.encore.byebuying.domain.UserRefreshToken;
import com.encore.byebuying.requestDto.UserFormRequest;
import com.encore.byebuying.repo.LocationRepo;
import com.encore.byebuying.repo.UserRefreshTokenRepo;
import com.encore.byebuying.service.WebClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.encore.byebuying.domain.Location;
import com.encore.byebuying.domain.User;
import com.encore.byebuying.service.UserService;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserResource {
    private final UserService userService;
    private final LocationRepo locationRepo;
    private final WebClientService webClientService;
    private final PasswordEncoder passwordEncoder;
    private final UserRefreshTokenRepo userRefreshTokenRepo;
    private final AuthenticationManager authenticationManager;
    private final AppProperties appProperties;

//    @PostMapping("/login")
//    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
//        );
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        return ResponseEntity.ok(new AuthResponse(tokenProvider.createToken(authentication)));
//
//    }

    @PostMapping("/user/save")
    public ResponseEntity<User> saveUser(@RequestBody UserFormRequest userFormRequest) {
        URI uri = URI.create(
                ServletUriComponentsBuilder
                        .fromCurrentContextPath()
                        .path("/api/user/save").toUriString());

        User newUser = userService.saveUser(userFormRequest);
//        webClientService.newUser(newUser.getUsername());

        return ResponseEntity.created(uri).body(newUser);
    }


    @GetMapping("/users") // ????????? ????????? ??????
    public ResponseEntity<Page<User>> getUsers(
            @RequestParam(required = false, defaultValue="1",value="page") int page) {
        Pageable pageable = PageRequest.of(page-1, 20, Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok().body(userService.getUsers(pageable));
    }

    @PostMapping("/user/getUser") // ?????? ????????? - ?????? or ??????
    public ResponseEntity<User> getUser(@RequestBody User userinfo) {
        User user = userService.getUser(userinfo.getUsername());
        System.out.println(userinfo.getUsername()+" "+userinfo.getPassword());
        if (passwordEncoder.matches(userinfo.getPassword(), user.getPassword())){
            return ResponseEntity.ok().body(user);
        }
        return ResponseEntity.badRequest().body(null);
    }

    @GetMapping("/user/getinfo") // ?????? ?????????
    public ResponseEntity<List<Location>> getUserLocation(@RequestParam String username) {
        List<Location> locations = new ArrayList<>(userService.getUser(username).getLocations());
        return ResponseEntity.ok().body(locations);
    }

    @GetMapping("/checkUser") // ????????? ?????? ?????? ??????
    public ResponseEntity<?> checkUser(
            @RequestParam(defaultValue = "", value = "username") String username) {
        boolean check = userService.checkUser(username);
        if (check) {
            return new ResponseEntity<>("SUCCESS", HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("FAIL", HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/user/delete") // ?????? ??????, ?????? ??? /api/user/getUser ?????? ?????? ??? ???????????? ??????
    public ResponseEntity<?> deleteUser(
    		@RequestParam(defaultValue = "", value="username") String username) {
        userService.deleteUser(username);
        return new ResponseEntity<>("SUCCESS", HttpStatus.OK);
    }

    @Transactional
    @PutMapping("/user/update") // ?????? ??????
    public ResponseEntity<?> updateUser(@RequestBody UserFormRequest userForm) {
        User user = userService.getUser(userForm.getUsername());
        if (user == null){
            return new ResponseEntity<>("FAIL", HttpStatus.OK);
        }
        if (userForm.getPassword() != null && !userForm.getPassword().equals("")) // ??????????????? ????????? ???
            user.setPassword(userForm.getPassword());
        user.setEmail(userForm.getEmail());
        user.setDefaultLocationIdx(userForm.getDefaultLocationIdx());
        
        // ???????????? ???????????? ????????? ??????
        List<Location> list = (List<Location>)user.getLocations();
        Long[] idList = new Long[list.size()];
        for(int i=0;i<list.size();i++)
        	idList[i]=list.get(i).getId();
        
        user.getLocations().clear();
        for(Long id : idList) {
        	locationRepo.deleteById(id);
        }
        
    	user.getLocations().addAll(userForm.getLocations());
        
        userService.updateUser(user);
        return new ResponseEntity<>("SUCCESS", HttpStatus.OK);
    }

    @GetMapping("/token/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                // request??? header??? "Bearer token~~~~" ???????????? ???????????? ????????? "Bearer " ?????? ??????
                String refresh_token = authorizationHeader.substring("Bearer ".length());
                // HMAC256 ??????
                Algorithm algorithm = Algorithm.HMAC256(appProperties.getAuth().getTokenSecret().getBytes());
                // verifier??? alhorithm??? ???????????? refreshToken??? ?????? ????????? ??????
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(refresh_token);

                String username = decodedJWT.getSubject();
                User user = userService.getUser(username);

                // DB??? ????????? refresh token??? ???????????? ??????
                UserRefreshToken userRefreshToken = userRefreshTokenRepo.findByUsername(username);
                if (userRefreshToken == null || !userRefreshToken.getRefreshToken().equals(refresh_token)) {
                    throw new RuntimeException("Refresh token is missing or miss match");
                }

                // ?????????????????? ????????? access token ????????? ??? ??????
                String access_token = JWT.create()
                        .withSubject(user.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis() + appProperties.getAuth().getAccesstokenExpiration())) // 10???
                        .withIssuer(request.getRequestURL().toString())
                        .withClaim("role", user.getRole().getCode())
                        .sign(algorithm); // ?????? ??????

                Map<String, String> tokens = new HashMap<>();
                tokens.put("access_token", access_token);
                tokens.put("refresh_token", refresh_token);

                response.setContentType(APPLICATION_JSON_VALUE);

                new ObjectMapper().writeValue(response.getOutputStream(), tokens);
            }catch (Exception exception) {
                response.setHeader("error", exception.getMessage());
                response.setStatus(FORBIDDEN.value());

                Map<String, String> error = new HashMap<>();
                error.put("error_message", exception.getMessage());
                response.setContentType(MimeTypeUtils.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
        } else {
            throw new RuntimeException("Refresh token is missing");
        }
    }
}

@Data
class RoleToUserForm {
    private String userid;
    private String rolename;
}

//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//class UserForm {
//    private String username;
//    private String password;
//    private String email;
//    private int defaultLocationIdx;
//    private Collection<Location> locations;
//
//    public User toEntity(){
//        return User.builder()
//                .username(this.username)
//                .password(this.password)
//                .email(this.email)
//                .defaultLocationIdx(this.defaultLocationIdx)
//                .locations(this.locations)
//                .build();
//    }
//}