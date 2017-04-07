package com.idzona.baud;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
    public static void main(String... args) {
        boolean test = false;

        int fond = 10;
        String arg = null;

        if(test) {
            System.out.println("*** Test mode BEGIN ***");

            try {
                Main main = new Main();

                File xls = new File("C:/projects/baud/ubb_p_a.xls");
                BaudData baudData = main.generateBaudEntries(xls);

                // Daily
                main.exportAll("daily.csv", baudData.getBaudEntries());

                // Weekly
                Set<BaudEntryPeriod> weekPeriods = main.generateWeekPeriods(baudData.getBaudEntries());
                main.exportBaudPeriodsCsv("weekly.csv", weekPeriods);
                main.exportBaudPeriodsJs("weekly.js", weekPeriods, baudData.getFond());
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println("*** Test mode END ***");
                System.exit(0);
            }
        }

        if(args.length <= 0) {
            System.out.print("Vavedete nomer na fond: ");
            Scanner sc = new Scanner(System.in);
            arg = sc.nextLine();
        } else {
            arg = args[0];
        }

        try {
            fond = Integer.parseInt(arg);
            if(fond < 0) {
                throw new NumberFormatException();
            }
        } catch(NumberFormatException e) {
            fond = 10;
            System.out.println("The mandatory 'fund' argument is invalid or missing. Using default value '10' (DSK Rastej)");
        }

//        System.out.println("Fund: " + fond);

        Main main = new Main();

        File xls = main.downloadFond(fond);

        if(xls == null) {
            System.out.println("Excel file is not available. Cannot continue");
            System.exit(-1);
        }

        BaudData baudData = null;

        try {
            baudData = main.generateBaudEntries(xls);
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

        main.exportAll("daily.csv", baudEntries);

        Set<BaudEntryPeriod> periodBauds = main.generateWeekPeriods(baudEntries);
        main.exportBaudPeriodsCsv("weekly.csv", periodBauds);
        main.exportBaudPeriodsJs("weekly.js", periodBauds, baudData.getFond());
    }

    private void exportBaudPeriodsJs(String fileName, Set<BaudEntryPeriod> periods, String fond) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(4);
        nf.setGroupingUsed(false);

        File f = new File(fileName);
        try(BufferedWriter bw = Files.newBufferedWriter(f.toPath(), Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            bw.write("o={};o.f='");
            bw.write(fond);
            bw.write("';o.b=[];o.e=[];");

            int i = 0;
            for(BaudEntryPeriod period : periods) {
                bw.write("o.b[" + i + "]={d:'");
                bw.write(formatter.format(period.getBaudBegin().getDate()) + "',n:");
                bw.write(nf.format(period.getBaudBegin().getTotalNav()) + ",p:");
                bw.write(nf.format(period.getBaudBegin().getSharePrice()) + ",s:");
                bw.write(nf.format(period.getBaudBegin().getTotalShares()) + "};");

                i++;
            }

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

//        System.out.println(nextWeek);

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
        // In worst case this will be the same as last => will not be included.
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
                bw.write(period.getBaudBegin().toCsvString());
                bw.write(',');
                bw.write(period.getBaudEnd().toCsvString());
                bw.newLine();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void exportAll(String fileName, Set<BaudEntryDay> baudEntries) {
        File f = new File(fileName);
        try(BufferedWriter bw = Files.newBufferedWriter(f.toPath(), Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            bw.write(BaudEntryDay.CSV_HEADER);
            bw.newLine();
//            System.out.println(BaudEntryDay.CSV_HEADER);
            for(BaudEntryDay baudEntryDay : baudEntries) {
                bw.write(baudEntryDay.toCsvString());
                bw.newLine();
//                System.out.println(baudEntryDay.toCsvString());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void analyze(Set<BaudEntryDay> baudEntries) {
        BaudEntryDay[] bauds = (BaudEntryDay[])baudEntries.toArray();

        double lastShares = bauds[bauds.length - 1].getTotalShares();

    }

    private BaudData generateBaudEntries(File xls) throws Exception {
        BaudData baudData = new BaudData();
        Set<BaudEntryDay> baudEntries = new TreeSet<>();
        baudData.setBaudEntries(baudEntries);

        FileInputStream fis = new FileInputStream(xls);

        Workbook wb = new HSSFWorkbook(fis);
        Sheet sheet = wb.getSheetAt(0);

        String name = sheet.getRow(1).getCell(0).getStringCellValue();
        baudData.setFond(name);

        for(int i = 4; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            String date = row.getCell(0).getStringCellValue();
//            System.out.println("*** DATE *** " + date);

            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            double totalNav = row.getCell(5).getNumericCellValue();
            double sharePrice = row.getCell(6).getNumericCellValue();

//            System.out.println("Date: " + localDate.toString() + ", totalNav: " + totalNav + ", sharePrice: " + sharePrice);

            BaudEntryDay baudEntryDay = new BaudEntryDay();
            baudEntryDay.setDate(localDate);
            baudEntryDay.setTotalNav(totalNav);
            baudEntryDay.setSharePrice(sharePrice);

//            System.out.println(baudEntryDay.toString());

            baudEntries.add(baudEntryDay);
        }

        wb.close();

        return baudData;
    }

    private File downloadFond(int fond) {
        File xls = null;
        String urlPattern = "http://baud.bg/listing/excel.php?start_date={start}&end_date={end}&fond={fond}";

        LocalDate dateEnd = LocalDate.now();
        LocalDate dateStart = dateEnd.minusYears(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String dateStartFormatted = dateStart.format(formatter);
        String dateEndFormatted = dateEnd.format(formatter);

//        System.out.println("Start date: " + dateStartFormatted);
//        System.out.println("End date: " + dateEndFormatted);

        String url = urlPattern.replace("{start}", dateStartFormatted)
                .replace("{end}", dateEndFormatted)
                .replace("{fond}", Integer.toString(fond));

//        System.out.println("XLS URL: " + url);

        try {
            xls = File.createTempFile("baud-", ".xls");
            xls.deleteOnExit();

            String tmpFileName = xls.getCanonicalPath();
//            System.out.println("Temporary file: " + tmpFileName);

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
