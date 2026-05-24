package ui;

import model.Model;

public interface UIEngine extends InteractionEngine {

    void onInit();

    void onDraw(Model model);

    void onDestroy();
}
