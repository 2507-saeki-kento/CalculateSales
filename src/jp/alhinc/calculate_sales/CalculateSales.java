package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}


		// listFilesを使用してfailesという配列に、
		//指定したパスに存在する全てのファイル(または、ディレクトリ)の情報を格納します。
		File[] files = new File(args[0]).listFiles();

		//先に売上ファイルのを格納する Listを宣言。
        List<File>rcdFiles = new ArrayList<>();

		//filesの数だけ繰り返すことで、
		//指定したパスが存在する全てのファイル(または、ディレクトリ)の数だけ繰り返されます。
		for(int i = 0; i < files.length ; i++) {

			//売り上げファイルを選別するために、名前を取得、変数に代入
			String fileName = files[i].getName();

			//matchesを使用してファイル名が「数字8桁.rcd」なのか判断します。
			//gatNameで習得した売り上げファイルから「数字8桁.rcd」になるようにふるいにかける
			if(fileName.matches("^[0-9]{8}.rcd$")) {
				//OKの時だけ、rcdFilesに追加
	 			rcdFiles.add(files[i]);
			}
		}



			//rcdFilesに複数の売り上げファイルの情報を格納しているので、その数だけ繰り返します。
			for(int i=0;i<rcdFiles.size();i++) {

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
					while((line = br.readLine()) != null) {
						//読んだら、Listに追加
						sales.add(line);
					}

				    //売上ファイルから読み込んだ売上金額をMapに加算していくために、型の変換を行います。
					//ファイルを開いて一つずつ探し出す必要がないから、上で書いたlist「sales」からgetする。
					//売上は配列だと1番にあたるから(sales.get(1))になった。
					long fileSale = Long.parseLong(sales.get(1));
					//読み込んだ売上⾦額を加算します。
					long saleAmount = branchSales.get(sales.get(0)) + fileSale;

					//加算した売上⾦額をMapに追加します。
					branchSales.put(sales.get(0),saleAmount);

				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
				} finally {
					// ファイルを開いている場合
					if(br != null) {
						try {
							// ファイルを閉じる
							br.close();
						} catch(IOException e) {
							System.out.println(UNKNOWN_ERROR);
						}
					}
				}

			}





		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
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
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while ((line = br.readLine()) != null) {
				//split を使って「,」(カンマ)で分割すること
				//items[0]には支店コード、items[1]には支店名が格納される
				String[] items = line.split(",");

				//Mapに追加する2つの情報をputの引数として指定する。
				branchNames.put(items[0], items[1]);
				branchSales.put(items[0], 0L);

				System.out.println(line);
			}

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

		return true;
	}

}