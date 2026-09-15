package cl.iplacex.examen.web;

import cl.iplacex.examen.inventario.InventarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static cl.iplacex.examen.web.LoginController.SESSION_USUARIO;

@Controller
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping("/inventario")
    public String verInventario(HttpSession session, Model model) {
        if (session.getAttribute(SESSION_USUARIO) == null) {
            return "redirect:/login";
        }
        model.addAttribute("productos", inventarioService.listarProductos());
        model.addAttribute("usuario", session.getAttribute(SESSION_USUARIO));
        return "inventario";
    }

    @PostMapping("/inventario/salida")
    public String registrarSalida(@RequestParam String sku,
                                   @RequestParam int cantidad,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        if (session.getAttribute(SESSION_USUARIO) == null) {
            return "redirect:/login";
        }
        try {
            inventarioService.registrarSalida(sku, cantidad);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/inventario";
    }
}
