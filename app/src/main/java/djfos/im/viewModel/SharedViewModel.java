package djfos.im.viewModel;


import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.lifecycle.ViewModel;
import djfos.im.model.Config;
import djfos.im.util.CustomMutableLiveData;

public class SharedViewModel extends ViewModel {
    public CustomMutableLiveData<Config> config = new CustomMutableLiveData<>();

    SharedViewModel() {
        config.setValue(new Config());
    }

    public void setThreshold(int progress) {
        config.getValue().setThreshold(progress);
    }

    public void setUri(String uri) {
        config.getValue().setUri(uri);
    }

    public void increace() {
        config.getValue().setThreshold(config.getValue().getThreshold() + 1);
    }




}
