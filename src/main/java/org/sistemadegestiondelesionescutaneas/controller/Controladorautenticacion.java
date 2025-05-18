import org.sistemadegestiondelesionescutaneas.service.Autenticacion;

@RestController
@RequestMapping("/auth")
public class Controladorautenticacion {

    private final Autenticacion autenticacionService; // Renombrar para claridad

    @Autowired
    public Controladorautenticacion(Autenticacion autenticacionService) { // Recibir el servicio
        this.autenticacionService = autenticacionService;
    }
}