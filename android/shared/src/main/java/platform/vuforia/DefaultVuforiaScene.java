package platform.vuforia;

import edu.hawhamburg.shared.misc.DefaultScene;
import edu.hawhamburg.shared.scenegraph.InnerNode;

/**
 * Dummy implementation of a scene with a Vuforia marker
 *
 * @author Philipp Jenke
 */
public class DefaultVuforiaScene extends DefaultScene {
    @Override
    public void onSetup(InnerNode rootNode) {
        VuforiaMarkerNode marker = new VuforiaMarkerNode("elphi");
        getRoot().addChild(marker);
        super.onSetup(marker);
    }
}
