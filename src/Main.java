import easyaccept.EasyAccept;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        String facade = "br.ufal.ic.p2.myfood.Facade";
        EasyAccept.main(new String[]{facade, "tests/Milestone 01/us1_1.txt"});
        EasyAccept.main(new String[]{facade, "tests/Milestone 01/us1_2.txt"});
        EasyAccept.main(new String[]{facade, "tests/Milestone 01/us2_1.txt"});
        EasyAccept.main(new String[]{facade, "tests/Milestone 01/us2_2.txt"});
//      EasyAccept.main(new String[]{facade, "tests/Milestone 01/us3_1.txt"});
//      EasyAccept.main(new String[]{facade, "tests/Milestone 01/us3_2.txt"});
//      EasyAccept.main(new String[]{facade, "tests/Milestone 01/us4_1.txt"});
//      EasyAccept.main(new String[]{facade, "tests/Milestone 01/us4_2.txt"});
    }
}


