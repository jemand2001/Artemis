from typing import Dict
from testUtils.AbstractTest import AbstractTest
from testUtils.Utils import printTester, getTesterOutput, clearTesterOutputCache
from testUtils.junit.Junit import Junit
from testUtils.junit.TestSuite import TestSuite
from time import time

class Tester:
    name: str = ""
    suite: TestSuite
    tests: Dict[str, AbstractTest] = dict()

    def __init__(self, name: str = "GBS-Tester-1.13"):
        self.name = name
        self.suite = TestSuite(name)

    def run(self):
        """
        Starts the tester and runs all tests added via "addTest(test: AbstractTest)".
        """

        printTester("Running: {}".format(self.name))

        # A dictionary of test results:
        # Test name -> result
        testResults: Dict[str, Result] = dict()

        for name, test in self.tests.items():
            # Reset the tester output cache:
            clearTesterOutputCache()

            if test.timeoutSec >= 0:
                printTester("Running test case '{}' with a {} second timeout...".format(name, test.timeoutSec))
            else:
                printTester("Running test case '{}' with no timeout...".format(name))
            test.start(testResults, self.suite)
            printTester("Finished test case '{}' in {} seconds.".format(name, test.case.time.total_seconds()))

            # Store the tester output in the test case:
            test.case.testerOutput = self.name + "\n" + getTesterOutput()
            # Update test results:
            testResults[name] = test.case.result
        self.__printResult()


    def addTest(self, test: AbstractTest):
        """
        Adds a new test that will be run once "run()" is invoked.
        """

        if test.name in self.tests:
            raise NameError("Test '{}' already registered. Test names should be unique!".format(test.name))
        self.tests[test.name] = test

    def __printResult(self):
        print("Result".center(50, "="))
        print("{} finished {} test cases in {} seconds.".format(self.name, len(self.tests), self.suite.time.total_seconds()))
        print("SUCCESS: {}".format(self.suite.successful))
        print("FAILED: {}".format(self.suite.failures))
        print("ERROR: {}".format(self.suite.errors))
        print("SKIPPED: {}".format(self.suite.skipped))
        print("".center(50, "="))

    def exportResult(self, outputPath: str):
        """
        Exports the test results into a JUnit format and stores it at the given outputPath.
        """

        junit: Junit = Junit(self.suite)
        junit.toXml(outputPath)