package tax;

public class testForTaxChar {
	public static void main(String[] args){
		TaxChar ppp9 = new TaxChar(32,14,25); 
		TaxChar ppp11 = new TaxChar(28,13,29);
		TaxChar ppp10 = new TaxChar(28,15,30);
		TaxChar ppp6 = new TaxChar(22,15,30);
		TaxChar ppp7 = new TaxChar(34,11,28);
		TaxChar ppp4 = new TaxChar(32,8,30);
		TaxChar ppp5 = new TaxChar(36,17,30);
		TaxChar ppp2 = new TaxChar(36,18,30);
		TaxChar ppp3 = new TaxChar(34,14,30);
		TaxChar ppp0 = new TaxChar(34,15,30);
		TaxChar ppp8 = new TaxChar(24,8,25);
		TaxChar ppp1 = new TaxChar(28,10,23);
		ppp7.setName("ppp7");
		ppp4.setName("ppp4");
		ppp1.setName("ppp1");
		System.out.println("normlized ppp7 is "+ ppp7);
		System.out.println("normlized ppp4 is "+ ppp4);
		System.out.println("normlized ppp1 is "+ ppp1);
		System.out.println("Distance between 11 and 10 is "+ppp11.EuclidianDistance(ppp10));
		System.out.println("Distance between 7 and 4 is "+ ppp7.EuclidianDistance(ppp4));
		System.out.println("Distance between 5 and 2 is "+ppp5.EuclidianDistance(ppp2));
		System.out.println("Distance between 3 and 0 is "+ppp3.EuclidianDistance(ppp0));
		System.out.println("Distance between 8 and 1 is "+ppp8.EuclidianDistance(ppp1));
		System.out.println("Distance between 11 and 6 is "+ppp11.EuclidianDistance(ppp6));
		System.out.println("Distance between 10 and 6 is"+ppp10.EuclidianDistance(ppp6));
		System.out.println("Distance between 11 and 9 is"+ppp11.EuclidianDistance(ppp9));
		System.out.println("Distance between 1 and 7 is"+ppp1.EuclidianDistance(ppp7));
		System.out.println("Distance between 4 and 8 is"+ppp4.EuclidianDistance(ppp8));
	}
}
