package co.edu.uceva.inventariosibe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Mapea rutas "limpias" (sin extensión .html) hacia los archivos estáticos
 * correspondientes usando forward, de modo que la URL del navegador se mantenga
 * limpia (p.ej. /login) pero internamente Spring sirva /login.html.
 *
 * El forward (a diferencia del redirect) NO cambia la URL en el navegador
 * y no genera una petición extra: Spring resuelve el recurso internamente.
 *
 * Esto es lo único necesario para tener URLs limpias con archivos .html planos
 * servidos desde src/main/resources/static/.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        forward(registry, "/", "forward:/login.html");
        forward(registry, "/login", "forward:/login.html");
        forward(registry, "/dashboard", "forward:/dashboard.html");
        forward(registry, "/inventario", "forward:/inventario.html");
        forward(registry, "/insumo-form", "forward:/insumo-form.html");
        forward(registry, "/lote-form", "forward:/lote-form.html");
        forward(registry, "/lotes", "forward:/lotes.html");
        forward(registry, "/movimiento-wizard", "forward:/movimiento-wizard.html");
        forward(registry, "/usuarios", "forward:/usuarios.html");
    }

    private void forward(ViewControllerRegistry registry, String urlPath, String viewName) {
        registry.addViewController(urlPath).setViewName(viewName);
    }
}