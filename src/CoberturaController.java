import guitesting.util.IOUtil;

import java.io.File;
import java.util.ArrayList;

import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.TouchCollector;

public class CoberturaController {
  File JAVA_HOME;
  File JAVA_PATH;

  public CoberturaController() {
    this(null);
  }

  public CoberturaController(String javaHome) {
    if (javaHome == null) {
      javaHome = System.getProperty("java.home");
    }
    String OSStr = System.getProperty("os.name");
    boolean isWindow = OSStr.toLowerCase().indexOf("windows") >= 0;
    if (javaHome == null)
      System.out.println("javaHome is null");

    JAVA_HOME = new File(javaHome);
    if (isWindow) {
      JAVA_PATH = new File(javaHome + "/bin/java.exe");
    } else {
      JAVA_PATH = new File(javaHome + "/bin/java");
    }
    if (JAVA_HOME.exists() && JAVA_PATH.exists()) {
      System.out.println("Cobertura controller is ready to use..");
    } else {
      System.err.println("JAVA_HOME path is not valid!");
    }
  }

  /**
   * @param basedir
   * @param datafile
   * @param destination
   * @param fileList
   * @param makeCleanFile
   *          make clean file if <code>true</code>, otherwise coverage data will be remained if the
   *          <code>destination</code> file exists.
   * @return
   */
  public static boolean instrument(String basedir, String datafile, String destination, String[] fileList,
      boolean makeCleanFile) {
    System.out.println(String.format("%s,%s,%s,%s,%s",basedir,datafile,destination,fileList,makeCleanFile));
    ArrayList<String> args = new ArrayList<String>();

    if (basedir == null || basedir.isEmpty())
      return false;

    args.add("--basedir");
    args.add(basedir);
    if (datafile != null) {
      args.add("--datafile");
      args.add(datafile);
    }
    if (destination != null) {
      args.add("--destination");
      args.add(destination);
    }
    if (fileList == null || fileList.length == 0)
      args.add("."); // instrument all files including ones within
    // sub-folders recursively
    else
      for (String classFile : fileList)
        args.add(classFile);

    if (makeCleanFile && datafile != null) {
      File destFile = new File(datafile);
      if (destFile.exists()) {
        if (!destFile.delete()) {
          System.err.println("Failed to make a clean file!");
        }
      }
    }
    net.sourceforge.cobertura.instrument.Main.main(args.toArray(new String[0]));
    return true;
  }

  public static boolean report(String datafile, String destination, String format, String encoding, String srcdir) {
    ArrayList<String> args = new ArrayList<String>();

    if (destination == null || destination.isEmpty())
      return false;

    if (datafile != null) {
      args.add("--datafile");
      args.add(datafile);
    }

    args.add("--destination");
    args.add(destination);

    if (format != null) {
      args.add("--format");
      args.add(format);
    }
    if (encoding != null) {
      args.add("--encoding");
      args.add(encoding);
    }

    if (srcdir != null) {
      args.add(srcdir);
    }
    try {
      net.sourceforge.cobertura.reporting.Main.main(args.toArray(new String[0]));
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public static void reportCoberturaData(String srcPath, String coverageFilePath, String reportFolderPath) {
    File reportFile = new File(reportFolderPath);
    if (reportFile.exists()) {
      IOUtil.deleteFolder(reportFile);
    }
    reportFile.mkdirs();

    report(coverageFilePath, reportFolderPath, "html", null, srcPath);
  }

  /**
   * return current coverage info. Accumulated coverage information is flushed.
   * 
   * @return collected coverage information.
   */
  public static ProjectData getAndInitCoverageInfo() {
    ProjectData result = new ProjectData();
    TouchCollector.applyTouchesOnProjectData(result);
    return result;
  }
}
