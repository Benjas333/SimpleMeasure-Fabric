package benjas333.simplemeasure.client;

import net.minecraft.world.phys.Vec3;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Utils {
    private static final DecimalFormat DecimalFormatter = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.ROOT));

    public static String formatNumber(double number) {
        return DecimalFormatter.format(number);
    }

    public static String formatNumber(int number) {
        return String.format(Locale.ROOT, "%,d", number);
    }

    public static double getVectorVolume(Vec3 vec3) {
        return Math.abs(vec3.x * vec3.y * vec3.z);
    }
}
