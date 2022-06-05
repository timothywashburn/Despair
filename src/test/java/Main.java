import java.text.DecimalFormat;

public class Main {
	public static void main(String[] args) {
		DecimalFormat format = new DecimalFormat("0.###");
		double total = 24;
		for(int i = 0; i < 4800; i++) {
			total -= 0.01 * total;
			total += 0.1;
			System.out.println(i + " " + format.format(total));
		}
	}
}
