package ca.mcgill.mcb.pcingola.snpSift;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.mcgill.mcb.pcingola.fileIterator.LineFileIterator;
import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.util.Timer;
import ca.mcgill.mcb.pcingola.vcf.LineChrPos;
import ca.mcgill.mcb.pcingola.vcf.VcfHeader;
import ca.mcgill.mcb.pcingola.vcf.VcfHeaderInfo;

/**
 * Sort VCF file/s by chromosome & position
 *
 * @author pablocingolani
 */
public class SnpSiftCmdSort extends SnpSift {

	public static final int SHOW = 1000;
	public static final int SHOW_LINES = 100 * SHOW;

	ArrayList<String> fileNames;
	VcfHeader vcfHeader;
	List<LineChrPos> vcfEntries;

	public SnpSiftCmdSort(String args[]) {
		super(args, "sort");
	}

	/**
	 * Load VCF files
	 */
	public void loadVcfFiles() {
		vcfEntries = new ArrayList<>();
		vcfHeader = null;

		// Iterate all files
		for (String file : fileNames) {
			if (verbose) Timer.showStdErr("Loading file '" + file + "'");
			processHeader(file);

			// Read the whole file
			LineFileIterator lfi = new LineFileIterator(file);
			for (String line : lfi)
				if (!line.startsWith("#")) {
					LineChrPos lineChrPos = new LineChrPos(line);
					vcfEntries.add(lineChrPos);
				}
		}
	}

	/**
	 * Parse command line arguments
	 */
	@Override
	public void parse(String[] args) {
		if (args.length == 0) usage(null);

		// Initialize
		fileNames = new ArrayList<String>();

		// Parse args
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (isOpt(arg)) usage("Unknown option '" + arg + "'");
			else fileNames.add(arg);
		}

		// Use STDIN if no input file is specified
		if (fileNames.size() <= 0) fileNames.add("-");
	}

	/**
	 * Read header and add missing headers
	 */
	void processHeader(String file) {
		// Open VCF file
		VcfFileIterator vcf = new VcfFileIterator(file);

		// Process header
		if (vcfHeader == null) vcfHeader = vcf.getVcfHeader();
		else {
			// Add missing headers
			for (VcfHeaderInfo vhi : vcfHeader.getVcfInfo()) {
				if (!vcfHeader.hasHeaderInfo(vhi)) vcfHeader.add(vhi);
			}
		}

		vcf.close();
	}

	/**
	 * Run
	 */
	@Override
	public void run() {
		loadVcfFiles();
		sort();
	}

	void sort() {
		Collections.sort(vcfEntries);

		// Show header
		System.out.println(vcfHeader);

		// Show lines
		for (LineChrPos lp : vcfEntries)
			System.out.println(lp.getLine());
	}

	/**
	 * Show usage message
	 */
	@Override
	public void usage(String msg) {
		if (msg != null) {
			System.err.println("Error: " + msg);
			showCmd();
		}

		showVersion();
		System.err.println("Usage: java -jar " + SnpSift.class.getSimpleName() + ".jar sort file.vcf [file_2.vcf ... file_N.vcf]");
		System.err.println("Note : If more than one file is given, files are merged and then sorted.");
		System.err.println("Note2: Loads the file/s in memory. Not suitable for large VCF files.");
		System.exit(1);
	}
}