package controller;

final class CharacterizationTests {

    static void run() {
        int passed = 0;
        passed += new ModelCharacterizationTests().run();
        passed += new ViewCharacterizationTests().run();
        passed += new ControllerCharacterizationTests().run();
        passed += new IntegrationCharacterizationTests().run();

        System.out.println("Passed " + passed + " characterization checks.");
    }
}
