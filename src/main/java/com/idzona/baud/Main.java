package com.idzona.baud;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class Main {
    private static final String URL_PATTERN = "http://baud.bg/listing/excel.php?start_date={start}&end_date={end}&fond={fond}";

    private Main() {}

    public static void main(String... args) {
        System.out.println("start");
        Main main = new Main();

        if (args.length == 0) { 
            main.loadFunds();
        } else {
            //TODO: funds.csv should be updated in cron by "python list-funds.py" 
            System.out.println("Funds are loaded from ./funds.csv now, no id supported");
        }
        System.exit(0);
        String arg;

        if(args.length <= 0) {
            System.out.print("Enter mutual fund ID: ");
            Scanner sc = new Scanner(System.in);
            arg = sc.nextLine();
        } else {
            arg = args[0];
        }

        // Test block.
        if(arg.equalsIgnoreCase("test")) {
            main.test();
            System.exit(0);
        }

        main.loadFund(arg, arg);
    }

    private void loadFund(String id, String fundPrefix) {
        int fundId;
        // Parse the fund ID. Use '10' as default value.
        try {
            fundId = Integer.parseInt(id);
            if(fundId < 0) {
                throw new NumberFormatException();
            }
        } catch(NumberFormatException e) {
            fundId = 10;
            System.out.println("The mandatory 'fundId' argument is invalid or missing. Using default value '10' (DSK Rastej)");
        }

        // Main processing.
        process(fundId, fundPrefix);
    }

    private void loadFunds() {
        try {
            InputStreamReader r = new InputStreamReader(new FileInputStream("funds.csv"));
            BufferedReader br = new BufferedReader(r);
            String line = null;
            int i = 0;
            while (null != (line = br.readLine())) {
                String[] parts = line.split(",");
                System.out.println(String.join(" --- ", parts));
                loadFund(parts[0], parts[2]);
                //if (++i == 3)
                //    break;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    private void process(int fundId, String fundPrefix) {
        File xlsFile = downloadFund(fundId);

        if(xlsFile == null) {
            System.out.println("Excel file is not available. Cannot continue");
            System.exit(-1);
        }

        BaudData baudData = null;

        try {
            baudData = generateBaudEntries(xlsFile);
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Set<BaudEntryDay> baudEntries = null;
        if(baudData != null) {
            baudEntries = baudData.getBaudEntries();
        }

        if(baudEntries == null || baudEntries.size() <= 0) {
            System.out.println("There are no entries to process.");
            System.exit(-1);
        }

        // Export daily.
        String prefix = "website/data/" + fundPrefix;
        exportDailyCsv(prefix + "-daily.csv", baudEntries);

        // Export weekly.
        Set<BaudEntryPeriod> periodBauds = generateWeekPeriods(baudEntries);
        exportBaudPeriodsCsv(prefix + "-weekly.csv", periodBauds);
        exportBaudPeriodsJs(prefix + ".js", periodBauds, baudData.getFundId());
    }

    private void test() {
        System.out.println("*** Test mode BEGIN ***");

        try {
            File xls = new File("C:/projects/baud/ubb_p_a.xls");
            BaudData baudData = generateBaudEntries(xls);

            // Daily
            exportDailyCsv("daily.csv", baudData.getBaudEntries());

            // Weekly
            Set<BaudEntryPeriod> weekPeriods = generateWeekPeriods(baudData.getBaudEntries());
            exportBaudPeriodsCsv("weekly.csv", weekPeriods);
            exportBaudPeriodsJs("weekly.js", weekPeriods, baudData.getFundId());
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("*** Test mode END ***");
        }
    }

    private void exportBaudPeriodsJs(String fileName, Set<BaudEntryPeriod> periods, String fond) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(4);
        nf.setGroupingUsed(false);

        File f = new File(fileName);
        try(BufferedWriter bw = Files.newBufferedWriter(f.toPath(), Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            bw.write("o={};o.f='" + fond + "';");

            bw.write("o.b=[];");
            int i = 0;
            for(BaudEntryPeriod period : periods) {
                bw.write("o.b[" + i + "]={d:'");
                bw.write(formatter.format(period.getBaudBegin().getDate()) + "',n:");
                bw.write(nf.format(period.getBaudBegin().getTotalNav()) + ",p:");
                bw.write(nf.format(period.getBaudBegin().getSharePrice()) + ",s:");
                bw.write(nf.format(period.getBaudBegin().getTotalShares()) + "};");
                i++;
            }

            bw.write("o.e=[];");
            i = 0;
            for(BaudEntryPeriod period : periods) {
                bw.write("o.e[" + i + "]={d:'");
                bw.write(formatter.format(period.getBaudEnd().getDate()) + "',n:");
                bw.write(nf.format(period.getBaudEnd().getTotalNav()) + ",p:");
                bw.write(nf.format(period.getBaudEnd().getSharePrice()) + ",s:");
                bw.write(nf.format(period.getBaudEnd().getTotalShares()) + "};");
                i++;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private Set<BaudEntryPeriod> generateWeekPeriods(Set<BaudEntryDay> baudEntries) {
        Set<BaudEntryPeriod> periodBauds = new TreeSet<>();

        BaudEntryDay[] bauds = new BaudEntryDay[baudEntries.size()];
        int i = 0;
        for(BaudEntryDay baudDay : baudEntries) {
            bauds[i] = baudDay;
            i++;
        }

        BaudEntryDay begin = bauds[0];
        BaudEntryDay end = bauds[0];

        int normalizer = begin.getDate().getDayOfWeek().getValue();
        LocalDate nextWeek = begin.getDate().minusDays(normalizer).plusWeeks(1);

        for(i = 1; i < bauds.length; i++) {
            BaudEntryDay current = bauds[i];

            if(current.getDate().isBefore(nextWeek)) {
                end = current;
                continue;
            }

            // Create new period...
            BaudEntryPeriod period = new BaudEntryPeriod();
            period.setBaudBegin(begin);
            period.setBaudEnd(end);
            periodBauds.add(period);

            // ...and reset loop check variables
            begin = current;
            end = current;
            normalizer = begin.getDate().getDayOfWeek().getValue();
            nextWeek = begin.getDate().minusDays(normalizer).plusWeeks(1);
        }

        // Handle edge case where we have less than 7 days for the last week.
        // In worst case this will be the same as last => will not be exported.
        BaudEntryPeriod period = new BaudEntryPeriod();
        period.setBaudBegin(begin);
        period.setBaudEnd(end);
        periodBauds.add(period);

        return periodBauds;
    }

    private void exportBaudPeriodsCsv(String fileName, Set<BaudEntryPeriod> periods) {
        File f = new File(fileName);
        try(BufferedWriter bw = Files.newBufferedWriter(f.toPath(), Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            bw.write(BaudEntryPeriod.CSV_HEADER);
            bw.newLine();
            for(BaudEntryPeriod period : periods) {
                bw.write(period.toCsvString());
                bw.newLine();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void exportDailyCsv(String fileName, Set<BaudEntryDay> baudEntries) {
        File f = new File(fileName);
        try(BufferedWriter bw = Files.newBufferedWriter(f.toPath(), Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            bw.write(BaudEntryDay.CSV_HEADER);
            bw.newLine();
            for(BaudEntryDay baudEntryDay : baudEntries) {
                bw.write(baudEntryDay.toCsvString());
                bw.newLine();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private BaudData generateBaudEntries(File xls) throws Exception {
        BaudData baudData = new BaudData();
        Set<BaudEntryDay> baudEntries = new TreeSet<>();
        baudData.setBaudEntries(baudEntries);

        FileInputStream fis = new FileInputStream(xls);

        Workbook wb = new HSSFWorkbook(fis);
        Sheet sheet = wb.getSheetAt(0);

        String name = sheet.getRow(1).getCell(0).getStringCellValue();
        baudData.setFundId(name);

        for(int i = 4; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            String date = row.getCell(0).getStringCellValue();

            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            double totalNav = row.getCell(5).getNumericCellValue();
            double sharePrice = row.getCell(6).getNumericCellValue();

            BaudEntryDay baudEntryDay = new BaudEntryDay();
            baudEntryDay.setDate(localDate);
            baudEntryDay.setTotalNav(totalNav);
            baudEntryDay.setSharePrice(sharePrice);

            baudEntries.add(baudEntryDay);
        }

        wb.close();

        return baudData;
    }

    private File downloadFund(int fond) {
        File xls = null;

        LocalDate dateEnd = LocalDate.now();
        LocalDate dateStart = dateEnd.minusYears(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String dateStartFormatted = dateStart.format(formatter);
        String dateEndFormatted = dateEnd.format(formatter);

        String url = URL_PATTERN.replace("{start}", dateStartFormatted)
                .replace("{end}", dateEndFormatted)
                .replace("{fond}", Integer.toString(fond));

        try {
            xls = File.createTempFile("baud-", ".xls");
            xls.deleteOnExit();

            URL xlsUrl = new URL(url);
            ReadableByteChannel rbc = Channels.newChannel(xlsUrl.openStream());
            FileOutputStream fos = new FileOutputStream(xls);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();

        } catch(Exception e) {
            e.printStackTrace();
        }

        return xls;
    }
}
