/**
 * TODO - READ ME
 * For running the tests do the following:
 *  1. Add MI6test file under bgu.spl.mics
 *  2. args[0] - Path for the main folder extracted from zip (which jsons and output folders inside)
 *  3. Since we are working with singleton in order to run this test
 *  please add 'clear' function for Diary and MessageBrokerImpl which will clear all the map and least (i.e. report.clear();)
 *  DO NOT forget to delete them after tests
 *
 * NOTE:All the json files generated using RANDOM parameters (with some basic logic).
 * They should be valid to your program, but output will not necessarily make sense (number of mission executed out of all,etc).
 * Therefore this tester design to find deadlock and flaws in your code. For performance compare the output result with friends (fullreport.json).
 * Feel free to add custom debugging prints such as:
 * "System.out.println(super.getName() + " Asking for gadget " + mission.getGadget());" (For M in example)
 */
