package Delirium;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Random;


/**
 * Класс Delirium умеет генерить файлы из набора заданных слов и ASCII символов.
 * Для работы необходимо создать экземпляр объекта.
 * Функциональность сосредоточена в перегруженном методе genFiles.
 * Простая реализация метода просто создаёт требуемое количество файлов.
 * Вторая реализация метода (с большим количеством параметров) после создания файлов ещё и выводит
 * статистику по созданным файлам.
 **/
class Delirium {
    //Не знаю что быстрее: вычисление символа при генерации, или получение из массива.
    //Тестов не проводил, но мне с массивом код кажется малость нагляднее.
    private static byte[] vParDelimiter = new byte[]{'\r', '\n'};
    private static char[] simbolsLowCase = new char[26];

    static {
        for (int iChar = 0; iChar < 26; ++iChar) {
            simbolsLowCase[iChar] = (char) ('a' + (char) iChar);
        }
    }

    //Передаваемые переменные хранятся в объекте, чтобы не гонять их по процедурам - так код менее громоздкий.
    private String fPath;
    private int fSentencesQty;
    //Вместо максимального количества предложений в жайле, я храню разницу между минимумом и максимумом.
    private int fSentencesQtyDelta;
    private String[] fWords;
    private int fProbably;
    //Объект генератора превдослучайных значений. Один на все случаи жизни.
    private Random fRand = new Random();

    //Конструктов
    public Delirium() {
    }

    /**
     * Простая реализация метода генерации файлов. Файлы создаются из ASCII символов и переданных слов.
     *
     * @param aPath        - путь для сохранения фаайлов
     * @param aFilesQty    - количество файлов, которые необходимо создать.
     * @param aSentenceQty - размер файлов в предложениях (какое количество предложений должно быть в каждом файле).
     * @param aWords       - заданный набор слов, которые могут встречаться в предложениях в файлах с некоторой вероятностью (см. ниже).
     * @param aProbably    - вероятность, что слово в предложении будет изменно из этого набора. (1 - 100%, 2 - 50%, 3 - 33% ...)
     **/
    public void genFiles(String aPath, int aFilesQty, int aSentenceQty, String[] aWords, int aProbably) {
        fProbably = Math.max(1, aProbably);
        fSentencesQty = Math.max(1, aSentenceQty);
        fWords = aWords;
        fPath = aPath;

        System.out.println("begin generate [" + aFilesQty + "] files with lenth [" + fSentencesQty + "]");
        for (int iFile = 0; iFile < aFilesQty; ++iFile) {
            genFile(genFileName(aPath, iFile), 0);
        }
    }

    /**
     * Усложнённая реализация метода генерации файлов. Файлы так же создаются из ASCII символов и переданных слов, но размер разных файлов разный (зависит от параметров aSentencesMin, aSentencesMax и vDiff). В конце работы выводит статистические данные по количесту файлов разных размеров (не более 10-ти позиций. Зависит от параметров aSentencesMin и aSentencesMax). Наиболее наглядной информация получается при разнице между aSentencesMin и aSentencesMax либо меньше 10-ти, либо в несколько десятков, иначе могут "вылазить рога", несколько вводят в заблуждение.
     *
     * @param aPath         - путь для сохранения фаайлов
     * @param aFilesQty     - количество файлов, которые необходимо создать.
     * @param aSentencesMin - минимальное количество предложений в файле.
     * @param aSentencesMax - максимальное количество предложений в файле.
     * @param vDiff         - переключатель распределения частоты файлов разной длины. 1 - выпуклая дуга, 2 - рандом, 3 - провал в середине.
     * @param aWords        - заданный набор слов, которые могут встречаться в предложениях в файлах с некоторой вероятностью (см. ниже).
     * @param aProbably     - вероятность, что слово в предложении будет изменно из этого набора. (1 - 100%, 2 - 50%, 3 - 33% ...)
     * @throws Exception
     **/
    public void genFiles(String aPath, int aFilesQty, int aSentencesMin, int aSentencesMax, int vDiff, String[] aWords, int aProbably) throws Exception {
        fProbably = Math.max(1, aProbably);
        fSentencesQty = Math.max(1, aSentencesMin);
        fSentencesQtyDelta = Math.max(0, aSentencesMax - fSentencesQty);
        fWords = aWords;
        fPath = aPath;

        int vFilesSizes[] = new int[aFilesQty];

        if (fSentencesQtyDelta <= 0) {
            System.out.println("begin generate [" + aFilesQty + "] files with lenth [" + fSentencesQty + "]");
            Arrays.fill(vFilesSizes, fSentencesQty);
        } else {
            //В данной секкции заполняется массив с размерами файлов (точнее с дельтой - приращением к минимальному значению).
            System.out.println("begin generate [" + aFilesQty + "] files with sizes in [" + fSentencesQty + " .. " + (fSentencesQty + fSentencesQtyDelta) + "]");
            switch (vDiff) {
                case 1:
                    double vK;

                    //Чем ближе к концу тем больше становится vK, и как результат сужается диапазон значений для генерации.
                    //Пошучается, что в конце списка файлов всё больше размеров средних. А поскольку средние размеры
                    //могли генериться и в начале списка, получается увеличение количества файлов со средними размерами.
                    for (int iFile = 0; iFile < aFilesQty; ++iFile) {
                        vK = (double) iFile / (double) aFilesQty;
                        vFilesSizes[iFile] = fRand.nextInt(fSentencesQtyDelta + 1 - (int) (fSentencesQtyDelta * 2.0d * vK / 3.0d)) + (int) (fSentencesQtyDelta * vK / 3);
                    }
                    break;

                case 2:
                    //Чистый рандом
                    for (int iFile = 0; iFile < aFilesQty; ++iFile) {
                        vFilesSizes[iFile] = fRand.nextInt(fSentencesQtyDelta + 1);
                    }
                    break;
                case 3:
                    //Чем дальше по списку файлов, тем больше vK, и как результат растёт значение vTemp.
                    //Ну и чем дальше по списку тем из меньшего диапазона выдаёт значение первый nextInt.
                    //Ну а по скольку vTemp растёт, то fRand.nextInt(2) == 0 в результате переключает между малым и большим размером файла.
                    System.out.println("diff = 3");
                    int vHalfDelta = fSentencesQtyDelta / 2;
                    int vTemp;

                    for (int iFile = 0; iFile < aFilesQty; ++iFile) {
                        vK = (double) iFile / (double) aFilesQty;
                        vTemp = (int) (vK * (double) (fSentencesQtyDelta - vHalfDelta));

                        vFilesSizes[iFile] = fRand.nextInt(fSentencesQtyDelta + 1 - vTemp - vHalfDelta) + ((fRand.nextInt(2) == 0) ? vTemp + vHalfDelta : 0);
                    }
                    break;

                default:
                    throw new Exception("Impossible variant of Diff value.");
            }
        }

        for (int iFile = 0; iFile < aFilesQty; ++iFile) {
            if (fSentencesQtyDelta > 0) {
//                System.out.println("File [" + iFile + "] size [" + (fSentencesQty + vFilesSizes[iFile]) + "]");
            }
            genFile(genFileName(aPath, iFile), vFilesSizes[iFile]);
        }

        printStatInfo(vFilesSizes);

    }

    private void printStatInfo(int[] aInfo) {
        int vLines = Math.min(10, fSentencesQtyDelta + 1);
        int vInfoLen = 50;
        int vPercentStep = 5;

        int vInfo[] = new int[vLines];
        Arrays.fill(vInfo, 0);

        for (int iFile = 0; iFile < aInfo.length; ++iFile) {
            vInfo[Math.min((int) (vLines * (double) aInfo[iFile] / (double) fSentencesQtyDelta), vLines - 1)] += 1;
        }

        System.out.println(Arrays.toString(vInfo));

        System.out.println("StatInfo");
        String vMaxAsterisks = "";
        for (int i = 0; i < vInfoLen; ++i) {
            vMaxAsterisks += '*';
        }

        int vMaxFiles = 0;
        for (int iLine = 0; iLine < vLines; ++iLine) {
            if (vMaxFiles < vInfo[iLine]) vMaxFiles = vInfo[iLine];
        }

        double vDTemp;
        NumberFormat vFormat = new DecimalFormat("#0.00");
        for (int iLine = 0; iLine < vLines; ++iLine) {
            vDTemp = (double) vInfo[iLine] / (double) vMaxFiles;
            System.out.println(vInfo[iLine] + " " + vFormat.format(vDTemp * 100) + "%");
            System.out.println(vMaxAsterisks.substring(vInfoLen - (int) (vInfoLen * vDTemp)));
        }
    }

    private String genFileName(String aPath, int aFileNo) {
        //Проверить существование папки
        //Если нет, создать
        //сделать заготовку имени
        //если файл существует увеличить индекс
        //и так по кругу, пока не найдётся свободный индекс

        //впрочем пока ...
        return aPath + File.separator + "Delirium" + aFileNo;
    }

    private void genFile(String aFileName, int aSizeDelta) {
        int vParaLen = 0;

        try (BufferedOutputStream vStream = new BufferedOutputStream(new FileOutputStream(aFileName))) {
            int vSentencesQty = fSentencesQty + aSizeDelta;
            for (int iSentence = 0; iSentence < vSentencesQty; ++iSentence) {
                //Между предложениями должны быть пробелы
                if (iSentence > 0) {
                    vStream.write(32); // writeUTF(" ");}

                    //Разбиваем текст на параграфы
                    if (vParaLen == 0) {
                        if (iSentence > 0) {
                            vStream.write(vParDelimiter);
                        }
                        vParaLen = fRand.nextInt(19) + 1;
                    }
                    --vParaLen;
                }

//                String vSentence = ""; //genSentence();
                vStream.write(genSentence().getBytes()); //writeUTF(vSentence);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String genSentence() {
        int vWords = fRand.nextInt(14) + 1;
        int vNextCommaIn = 0;
        String vSentence = "";


        for (int iWord = 0; iWord < vWords; ++iWord) {
            if (iWord != 0) {
                if (vNextCommaIn == 0) {
                    vSentence += ',';
                    vNextCommaIn = fRand.nextInt(20);
                } else {
                    --vNextCommaIn;
                }
                vSentence += ' ';
            } else vNextCommaIn = fRand.nextInt(20);

            vSentence += genWorld();
        }

        int vSign = fRand.nextInt(100);

        vSentence += vSign < 80 ? '.' : (vSign < 95 ? "!" : '?');
        return (new String() + vSentence.charAt(0)).toUpperCase() + vSentence.substring(1);
    }

    private String genWorld() {
        if (fRand.nextInt(fProbably) == 0) {
            return fWords[fRand.nextInt(fWords.length)];
        } else {
            String vWord = "";
            int vLen = fRand.nextInt(14) + 1;

            for (int iChar = 0; iChar < vLen; ++iChar) {
                vWord += simbolsLowCase[fRand.nextInt(simbolsLowCase.length)];
            }

            return vWord;
        }
    }
}
