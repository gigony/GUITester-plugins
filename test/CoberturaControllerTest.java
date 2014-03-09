import org.junit.Test;

public class CoberturaControllerTest {

  @Test
  public void test() {
    CoberturaController cobertura = new CoberturaController();
    String[] applications = new String[] { "TerpPaint5_n", "TerpSpreadSheet5_n", "TerpPresent5_n", "rachota_n",
        "jabref_n", "freemind_n", "jpdftweak_n" };
    // String[] applications=new String[]{"jabref_n"};
    for (String app : applications) {
      // System.out.println("G:\\Programming\\github\\GUITester\\GUITester\\Experiments\\"+app.replace("_n","")+"\\src\\");
      // System.out.println("G:\\Programming\\Graduation_data\\"+app+"\\ripperCoverage.ser");
      // cobertura.report("G:\\Programming\\Graduation_data\\"+app+"\\ripperCoverage.ser","G:\\Programming\\Graduation_data\\"+app+"\\report_ripper\\","html","euc-kr","G:\\Programming\\github\\GUITester\\GUITester\\Experiments\\"+app.replace("_n","")+"\\src\\");
      // cobertura.report("G:\\Programming\\Graduation_data\\"+app+"\\ripperCoverage.ser","G:\\Programming\\Graduation_data\\"+app+"\\report_ripper\\","html","utf-8","G:\\Programming\\Graduation_data\\"+app+"\\src\\");

    }
  }

  @Test
  public void testInstrument() throws Exception {

    // String[] applications = new String[] { "TerpPaint5", "TerpSpreadSheet5", "TerpPresent5", "rachota",
    // "jabref", "freemind", "jpdftweak" };
    String[] applications = new String[] { "TerpPaint5" };
    for (String app : applications) {
      CoberturaController.instrument("/Users/gigony/git/guitester-benchmark/" + app + "/bin",
          "/Users/gigony/git/guitester-benchmark/" + app + "/cleanCoverage.ser",
          "/Users/gigony/git/guitester-benchmark/" + app + "/instrumented", null, true);
    }

  }

}
