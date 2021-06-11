package tax;

public class TestForUPGMA {
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
		UPGMA upgma = new UPGMA();
		upgma.addTC(ppp0);
		upgma.addTC(ppp1);
		upgma.addTC(ppp2);
		upgma.addTC(ppp3);
		upgma.addTC(ppp4);
		upgma.addTC(ppp5);
		upgma.addTC(ppp6);
		upgma.addTC(ppp7);
		upgma.addTC(ppp8);
		upgma.addTC(ppp9);
		upgma.addTC(ppp10);
		upgma.addTC(ppp11);
		upgma.calUPGMA();
		upgma.printTaxaPair();
	}
}
