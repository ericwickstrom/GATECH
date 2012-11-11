package scannergenerator;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class RDPControl {
	DefinedClass[] classes;
	RDP parser;
	static int tokenindex;
	private int i;

	public String generateRegex(String filename) {
		int index = 0, index2 = 0;

		RDPControl controller = new RDPControl();
		controller.classes = controller.ReadClassesFromFile(filename);
		controller.parser = new RDP();

		while (controller.classes[index] != null) {
			System.out.println("\n\n");
			controller.parser.regex = "";
			controller.parser.index = 0;
			controller.parser.classes = controller.classes;
			controller.parser.inputBuffer = controller.classes[index]
					.getDefinition();
			String out = controller.parser.regEx();
			controller.classes[index].setRegex(out.toCharArray());
			index += 1;
		}
		while (controller.classes[index2] != null) {
			controller.classes[index2].setRegex(parseFinal(
					new String(controller.classes[index2].getRegex()))
					.toCharArray());
			index2++;
		}
		DefinedClass[] copyofrange = Arrays.copyOfRange(controller.classes,
				tokenindex, controller.classes.length - 1);
		String merged = mergeDefinedClasses(copyofrange);
		return merged;
	}

	public static void main(String[] args) {
		String filename = "doc/sample_input_specification.txt";
		int index = 0;
		RDPControl controller = new RDPControl();
		controller.classes = controller.ReadClassesFromFile(filename);
		controller.parser = new RDP();
		while (controller.classes[index] != null) {
			controller.parser.regex = "";
			controller.parser.index = 0;
			controller.parser.classes = controller.classes;
			controller.parser.inputBuffer = controller.classes[index]
					.getDefinition();
			String out = controller.parser.regEx();
			System.out.println(out);
			System.out.println(controller.parseFinal(out));
			controller.classes[index].setRegex(out.toCharArray());
			index += 1;
		}
	}

	public static List<DefinedClass> getOutput(String filename) {
		if (filename.equals("")) {
			filename = "doc/sample_input_specification.txt";
		}

		int index = 0;
		RDPControl controller = new RDPControl();
		controller.classes = controller.ReadClassesFromFile(filename);
		controller.parser = new RDP();
		while (controller.classes[index] != null) {
			controller.parser.regex = "";
			controller.parser.index = 0;
			controller.parser.classes = controller.classes;
			controller.parser.inputBuffer = controller.classes[index]
					.getDefinition();
			String out = controller.parser.regEx();
			controller.classes[index].setRegex(out.toCharArray());
			index += 1;
		}
		ArrayList<DefinedClass> al = new ArrayList<DefinedClass>();
		for (DefinedClass c : controller.classes) {
			if (c != null && c.isAToken) {
				String finalRegex = controller.parseFinal(String.valueOf(c
						.getRegex()));
				c.setRegex(finalRegex.toCharArray());
				al.add(c);
			}
		}
		return al;
	}

	public static DefinedClass[] ReadClassesFromFile(String filename) {
		DefinedClass[] output = new DefinedClass[100];
		int index = 0;
		try {
			FileInputStream fs = new FileInputStream(filename);
			DataInputStream ds = new DataInputStream(fs);
			BufferedReader br = new BufferedReader(new InputStreamReader(ds));
			String dataIn;
			boolean tokenTown = false;
			while ((dataIn = br.readLine()) != null) {
				if (dataIn.startsWith("%%") || dataIn.trim().isEmpty()) {
					tokenindex = index;
					tokenTown = true;
					continue;
				} else if (dataIn.startsWith("$")) {
					String[] bits = dataIn.split("\\s+");

					String d = "";

					for (int i = 1; i < bits.length; i++) {
						d = d + bits[i];
					}
					char[] def = new char[d.length()];
					d.getChars(0, d.length(), def, 0);
					output[index] = new DefinedClass(bits[0], def, tokenTown);
					index += 1;
				} else {
					System.out.println("Unexpected line in file: " + dataIn);
				}
			}
			return output;
		} catch (Exception ex) {
			System.out.println("Could not read from file: " + ex.getMessage());
			return null;
		}
	}

	public String myParser(String s) {
		i = 0;
		while (i < s.length()) {
			int u = 0;
			int v = 0;
			if (s.charAt(i) == ']') {
				u = s.lastIndexOf('[', i);
				v = i + 1;
				String sub = s.substring(u, v);

				String[] strs = sub.split("-");
				String[] ls = new String[strs.length - 1];
				String fin = "";
				boolean split = false;
				int count = -1;
				for (int i = 0; i < ls.length; i++) {
					if (i > 0) {
						ls[i] = "|[" + strs[i].charAt(strs[i].length() - 1)
								+ "-" + strs[i + 1].charAt(0) + "]";
						split = true;
					} else
						ls[i] = "[" + strs[i].charAt(strs[i].length() - 1)
								+ "-" + strs[i + 1].charAt(0) + "]";
					fin += ls[i];
					count++;
				}
				if (split) {
					i -= 3 * count;
					s = s.substring(0, u) + fin + s.substring(v);
					continue;
				}
				sub = expand(sub);
				s = s.substring(0, u) + sub + s.substring(v);
			}
			i++;
		}
		return s;
	}

	private String expand(String sub) throws StringIndexOutOfBoundsException {
		// TODO Auto-generated method stub
		char lb = sub.charAt(1);
		char ub = sub.charAt(sub.length() - 2);
		int indx = 1;
		int l = sub.length();

		while (indx < sub.length() - 1) {
			if (lb == ub) {
				sub = "(" + sub.substring(1, indx)
						+ sub.substring(indx + 1, sub.length() - 1) + ")";
				i += indx - 2;
				return sub;
			} else if (indx == 1) {
				sub = sub.substring(0, indx) + lb + "|"
						+ sub.substring(indx + 1, sub.length());
				lb += 1;
				indx += 2;
			} else {
				sub = sub.substring(0, indx) + lb + "|"
						+ sub.substring(indx, sub.length());
				lb += 1;
				indx += 2;
			}
		}

		i += indx;
		return sub;
	}

	public String parseFinal(String s) {
		String f = new String();
		char ch, nch;
		boolean flag = false;

		for (int i = 0; i < s.length();) {
			if (s.charAt(i) == '\\') {
				f = f.concat(s.substring(i, i + 2));
				i += 2;
			} else if ((i + 1) < s.length() && s.charAt(i) == '['
					&& s.charAt(i + 1) != '^') {
				f = f.concat("(" + s.substring(i, i + 4));
				i += 4;
				while (((i) < s.length()) && (s.charAt(i) != ']')) {
					flag = true;
					f = f.concat("]|[" + s.substring(i, i + 3));
					i += 3;
				}
				f = f.concat("])");
				i++;
			} else if ((i + 2) < s.length()
					&& s.substring(i, i + 2).equals("[^")) {

				if (s.charAt(i + 3) == '-') {
					char rlb = s.charAt(i + 2);
					char rub = s.charAt(i + 4);

					List<Character> accepts = new ArrayList<Character>();

					i += 9;
					while (s.charAt(i) != ']') { // [^A-C]IN[A-La-z]

						for (char _ch = s.charAt(i); _ch <= s.charAt(i + 2); _ch++)
							if (_ch < rlb || _ch > rub)
								accepts.add(((Character) _ch));

						i += 3;
					}

					f = f.concat("(");
					for (int k = 0; k < accepts.size() - 1; k++)
						f = f.concat(String.valueOf(accepts.get(k)) + "|");
					f = f.concat(accepts.get(accepts.size() - 1) + ")");

					i += 1;

				} else { // Simple case
					f = f.concat("(");
					nch = s.charAt(i + 2);
					for (ch = s.charAt(i + 7); ch < s.charAt(i + 9); ch++)
						if (ch != nch)
							f = f.concat(String.valueOf(ch) + "|");
					ch = s.charAt(i + 9);
					if (ch != nch)
						f = f.concat(String.valueOf(ch) + ")");
					i += 11;
				}

			} else if (s.charAt(i) == '+') {
				if (s.charAt(i - 1) == ']') {
					String sub = s.substring(s.lastIndexOf("[", i), i);
					f = f + "(" + sub + ")*";
					i += sub.length();
				} else if (s.charAt(i - 1) == ')')
					f = f + s.substring(s.lastIndexOf("(", i), i) + "*";
				else
					f = "(" + f + "(" + s.substring(i - 1, i) + ")" + "*)";
				i++;
			} else {
				f = f.concat(((Character) s.charAt(i)).toString());
				i++;
			}
		}

		return f;
	}

	private String mergeDefinedClasses(DefinedClass[] classes) {
		String s;
		s = "";
		for (DefinedClass c : classes) {
			if (c == null)
				return s.substring(0, s.length() - 1);
			System.out.println(c.getRegex());
			s += "(" + (new String(c.getRegex())) + ")|";
		}
		return s.substring(0, s.length() - 1);
	}

}
