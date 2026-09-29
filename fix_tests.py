import re

with open('src/test/java/com/booking/service/ReservationServiceTest.java', 'r') as f:
    content = f.read()

# Add SecurityContext imports
imports = '''
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.booking.security.SecurityUtils;
import org.mockito.MockedStatic;
import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
'''
content = content.replace('import java.util.Optional;', 'import java.util.Optional;\n' + imports)

# We need to set SecurityContextHolder in setUp
setup_code = '''
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
'''
content = content.replace('void setUp() {', 'void setUp() {' + setup_code)

# Replace old method calls
content = re.sub(r'reservationService\.createReservation\(([^,]+), "[^"]+"\)', r'reservationService.createReservation(\1)', content)
content = re.sub(r'reservationService\.createReservation\(([^,]+), "user"\)', r'reservationService.createReservation(\1)', content)

content = re.sub(r'reservationService\.getReservationById\(([^,]+), "[^"]+", (true|false)\)', r'reservationService.getReservationById(\1)', content)
content = re.sub(r'reservationService\.updateReservation\(([^,]+), ([^,]+), "[^"]+", (true|false)\)', r'reservationService.updateReservation(\1, \2)', content)

# Write back
with open('src/test/java/com/booking/service/ReservationServiceTest.java', 'w') as f:
    f.write(content)

print('Replaced')
