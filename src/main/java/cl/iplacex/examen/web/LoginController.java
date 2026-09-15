package cl.iplacex.examen.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Autenticacion minima para efectos de demostracion (Actividad 2/3: sobre
 * esta pantalla corren las pruebas de integracion y de aceptacion).
 *
 * Nota: las credenciales estan fijas a proposito -- este proyecto es un
 * caso de estudio academico, no un sistema en produccion.
 */
@Controller
public class LoginController {

    static final String SESSION_USUARIO = "usuario";
    private static final String USUARIO_DEMO = "qa.tester";
    private static final String CLAVE_DEMO = "Test1234";

    @GetMapping("/login")
    public String formularioLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String usuario,
                                 @RequestParam String clave,
                                 HttpSession session,
                                 Model model) {
        if (USUARIO_DEMO.equals(usuario) && CLAVE_DEMO.equals(clave)) {
            session.setAttribute(SESSION_USUARIO, usuario);
            return "redirect:/inventario";
        }
        model.addAttribute("error", "Usuario o clave incorrectos");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
