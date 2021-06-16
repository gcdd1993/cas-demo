import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

/**
 * @author gcdd1993
 */
public class PasswordEncoderTest {

    public static void main(String[] args) {
        String password = "aaa123456";
        PasswordEncoder passwordEncoder = new Pbkdf2PasswordEncoder(
                "e561a4e6-c82c-11eb-b8bc-0242ac130003",
                180000,
                256);
        System.out.println(passwordEncoder.encode(password));
    }
}
