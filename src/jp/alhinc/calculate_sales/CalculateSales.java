package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";
	private static final String FILE_NOT_SEQUENTIAL = "売上ファイル名が連番になっていません";
	private static final String OVER_THE_10_DIGITS = "合計金額が10桁を超えました";
	private static final String KEY_NOT_EXIST = "の支店コードが不正です";
	private static final String INVALID_FORMAT = "のフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();
		//売り上げファイルの読み込み
		BufferedReader br = null;
		if (args.length != 1) {
			//コマンドライン引数が1つ以上か以下の設定の場合は、
			//エラーメッセージをコンソールに表示します。
			System.out.println(UNKNOWN_ERROR);
			return;
		}

		// 支店定義ファイル読み込み処理
		if (!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// listFilesを使用してfailesという配列に、
		//指定したパスに存在する全てのファイル(または、ディレクトリ)の情報を格納します。
		File[] files = new File(args[0]).listFiles();

		//先に売上ファイルのを格納する Listを宣言。
		List<File> rcdFiles = new ArrayList<>();

		//filesの数だけ繰り返すことで、
		//指定したパスが存在する全てのファイル(または、ディレクトリ)の数だけ繰り返されます。
		for (int i = 0; i < files.length; i++) {

			//売り上げファイルを選別するために、名前を取得、変数に代入
			String fileName = files[i].getName();

			//matchesを使用してファイル名が「数字8桁.rcd」なのか判断します。
			//gatNameで習得した売り上げファイルから「数字8桁.rcd」になるようにふるいにかける
			//対象がファイルであり、「数字8桁.rcd」なのか判定します。
			if (files[i].isFile() && fileName.matches("^[0-9]{8}[.]rcd$")) {
				//OKの時だけ、rcdFilesに追加
				rcdFiles.add(files[i]);
			}

		}

		Collections.sort(rcdFiles);
		//比較回数は売上ファイルの数よりも1回少ないため、
		//繰り返し回数は売上ファイルのリストの数よりも1つ小さい数です。
		for (int i = 0; i < rcdFiles.size() - 1; i++) {

			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));

			//比較する2つのファイル名の先頭から数字の8文字を切り出し、int型に変換します。
			//2つのファイル名の数字を比較して、差が1ではなかったら、
			//エラーメッセージをコンソールに表示します。
			if ((latter - former) != 1) {
				System.out.println(FILE_NOT_SEQUENTIAL);
				return;
			}
		}

		//rcdFilesに複数の売り上げファイルの情報を格納しているので、その数だけ繰り返します。
		for (int i = 0; i < rcdFiles.size(); i++) {

			//支店定義ファイル読み込み(readFilesメゾット)を参考に売り上げファイルの中身を読み込みます。
			//売り上げファイルの1行目には支店コード、２行目には売上金額が入っています。
			try {

				//読むために、まず一つファイルを開く
				File file = new File(args[0], rcdFiles.get(i).getName());
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);

				String line;

				ArrayList<String> sales = new ArrayList<>();

				//nullじゃない限り、読み続ける、読んだものは一回lineに入る
				while ((line = br.readLine()) != null) {

					//読んだら、Listに追加
					sales.add(line);
				}
				if (sales.size() != 2) {
					//売上ファイルの⾏数が2⾏ではなかった場合は、
					//エラーメッセージをコンソールに表⽰します。
					System.out.println(file.getName() + INVALID_FORMAT);
					return;
				}
				if (!branchNames.containsKey(sales.get(0))) {
					System.out.println(file.getName() + KEY_NOT_EXIST);
					return;
					//⽀店情報を保持しているMapに売上ファイルの⽀店コードが存在しなかった場合は、
					//エラーメッセージをコンソールに表⽰します。
				}
				//売上金額が数字ではなかった場合は、
				//エラーメッセージをコンソールに表示します。
				if (!sales.get(1).matches("^[0-9]*$")) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}
				//売上ファイルから読み込んだ売上金額をMapに加算していくために、型の変換を行います。
				//ファイルを開いて一つずつ探し出す必要がないから、上で書いたlist「sales」からgetする。
				//売上は配列だと1番にあたるから(sales.get(1))になった。
				long fileSale = Long.parseLong(sales.get(1));
				//読み込んだ売上金額を加算します。
				long saleAmount = branchSales.get(sales.get(0)) + fileSale;
				//11桁以上になったらエラーメッセージを表示し処理を終了する。
				if (saleAmount >= 10000000000L) {
					System.out.println(OVER_THE_10_DIGITS);
					return;
				}
				//加算した売上金額をMapに追加します。
				branchSales.put(sales.get(0), saleAmount);

			} catch (IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				// ファイルを開いている場合
				if (br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch (IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}

		}

		// 支店別集計ファイル書き込み処理
		if (!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);

			//支店定義ファイルが存在しない場合、コンソールにエラーメッセージを表示します。
			if (!file.exists()) {
				System.out.println(FILE_NOT_EXIST);
				return false;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			String[] items;
			// 一行ずつ読み込む
			while ((line = br.readLine()) != null) {
				//split を使って「,」(カンマ)で分割すること
				//items[0]には支店コード、items[1]には支店名が格納される
				items = line.split(",");

				//支店定義ファイルの仕様が満たしているかふるいにかける
				if ((items.length != 2) || (!items[0].matches("^[0-9]{3}$"))) {
					System.out.println(FILE_INVALID_FORMAT);
					return false;
				}
				//Mapに追加する2つの情報をputの引数として指定する。
				branchNames.put(items[0], items[1]);
				branchSales.put(items[0], 0L);
			}
			//エラーメッセージをコンソールに表示します。
		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		//ファイルを作る
		BufferedWriter bw = null;

		//書き込む準備をした
		try {
			File file = new File(path, fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			//拡張構文でkeyの一覧を取得
			for (String key : branchNames.keySet()) {

				//writeメソッドで書き込んでいる
				bw.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));
				bw.newLine();
			}

		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}

		return true;
	}
}