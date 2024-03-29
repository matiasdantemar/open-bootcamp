//Aqui tambien se pueden generar excepciones, en algun metodo
package com.example.obspringsecurityjwtroles.controller;

import com.example.obspringsecurityjwtroles.config.TokenProvider;
import com.example.obspringsecurityjwtroles.dto.AuthToken;
import com.example.obspringsecurityjwtroles.dto.LoginUser;
import com.example.obspringsecurityjwtroles.entities.User;
import com.example.obspringsecurityjwtroles.dto.UserDto;
import com.example.obspringsecurityjwtroles.exception.EmailAlreadyExistsException;
import com.example.obspringsecurityjwtroles.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/users")
public class  UserController {

    private AuthenticationManager authenticationManager;
    private TokenProvider jwtTokenUtil;
    private UserService userService;

    public UserController(AuthenticationManager authenticationManager, TokenProvider jwtTokenUtil, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
    }

    //LOGEARSE, permite autenticarse
    @PostMapping("/authenticate")
    public ResponseEntity<?> generateToken(@RequestBody LoginUser loginUser) throws AuthenticationException { // recibe un objeto LoginUser que brinda usuario y contraseña

        // utiliza la clase de spring authenticationManager para poder cargar esos datos, que le sirvan a spring para hacer la autenticacion, para verificar que ese usuario existe
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginUser.getUsername(),
                        loginUser.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication); // ese usuario lo carga en el contexto de seguridad de spring
        final String token = jwtTokenUtil.generateToken(authentication); // gener aun token JWT si es que sale bien
        return ResponseEntity.ok(new AuthToken(token)); // el token lo retorna como respuesta en JSON, lo genera
    }

    // Punto de entrada a la aplicacion para REGISTRO
    // @RequestMapping(value="/register", method = RequestMethod.POST)      ASI ERA ANTES
    @PostMapping("/register")
    public User saveUser(@RequestBody UserDto user){
        // prueba a lanzar excepción customizada
        //throw new EmailAlreadyExistsException("Email ocupado");
        return userService.save(user);
    }


    // Controladroes con anotaciones para controlar los roles, para limitar el acceso
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/hello-admin")
    public String adminPing(){
        return "Only Admins Can Read This";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/hello-admin-user")
    public String adminUser(){
        return "Only Admins and Users Can Read This";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/hello-user")
    public String userPing(){
        return "Any User Can Read This";
    }

}
