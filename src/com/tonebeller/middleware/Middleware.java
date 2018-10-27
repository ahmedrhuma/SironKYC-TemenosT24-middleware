package com.tonebeller.middleware;

public class Middleware {

	public static void main(String[] args) {
		long startTime = System.nanoTime();
		try {
			int len = args.length;
			if (len < 4){
				System.out.println("\n Parameters are required.\n");
				System.out.println("\n [0] KYC_ROOT.\n");
				System.out.println("\n [1] Client.\n");
				System.out.println("\n [2] Online subfolder.\n");
				System.out.println("\n [3] Config PATH.\n");
				System.exit(-1);
			}
			Config config = Config.getInstance();
//			 setters
			config.setKYCRoot(args[0]).setClient(args[1]).setSubfolder(args[2]).setPath(args[3]).load();
			System.out.println("\n---------- Integration middleware started ---------- \n"
					+ Middleware.class.getName());
			Customer customer = new Customer(config);
			config.setCustomer(customer);
			new Webservice();
		}
		catch(Exception e){
			System.out.println("\n\n== Error in middleware: ==\n" + e.getMessage() + "\n\n");
			System.exit(-1);
		}
		finally {
			long stopTime = System.nanoTime();
			long elapsedTime = stopTime - startTime;
			System.out.println("Time Elapsed to fully execute middleware: " + (double)elapsedTime / 1000000000.0 + " seconds.");
		}
	}

}
