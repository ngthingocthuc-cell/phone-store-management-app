package Utils;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.*;
import java.util.Base64;

public final class Passwords {
    private Passwords() {}
    public static String hash(String password) {
        byte[] salt=new byte[16];new SecureRandom().nextBytes(salt);
        return "pbkdf2$120000$"+Base64.getEncoder().encodeToString(salt)+"$"+Base64.getEncoder().encodeToString(derive(password,salt,120000));
    }
    public static boolean matches(String password,String stored) {
        if(password==null||stored==null)return false;
        if(!stored.startsWith("pbkdf2$"))return MessageDigest.isEqual(password.getBytes(java.nio.charset.StandardCharsets.UTF_8),stored.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        try{String[] parts=stored.split("\\$");return MessageDigest.isEqual(derive(password,Base64.getDecoder().decode(parts[2]),Integer.parseInt(parts[1])),Base64.getDecoder().decode(parts[3]));}catch(RuntimeException e){return false;}
    }
    private static byte[] derive(String value,byte[] salt,int count) {
        PBEKeySpec spec=new PBEKeySpec(value.toCharArray(),salt,count,256);
        try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();}catch(GeneralSecurityException e){throw new IllegalStateException(e);}finally{spec.clearPassword();}
    }
}
