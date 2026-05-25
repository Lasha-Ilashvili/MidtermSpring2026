package controller;

final class CharacterizationTests {

    static void run() {
        int passed = 0;
        passed += new GameCharacterizationTests().run();
        passed += new CliCharacterizationTests().run();
        passed += new PlayerPromptCharacterizationTests().run();
        passed += new IntegrationCharacterizationTests().run();

        System.out.println("Passed " + passed + " characterization checks.");
    }
}
