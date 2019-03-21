package djfos.im.ui;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import djfos.im.R;
import djfos.im.databinding.FragmentHomeBinding;
import djfos.im.viewModel.SharedViewModel;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class HomeFragment extends Fragment {
    FragmentHomeBinding binding;
    private SharedViewModel model;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);


        binding.getRoot().<Button>findViewById(R.id.button_choose_image).setOnClickListener((view) -> {
            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_adjustPage);
        });




        return binding.getRoot();
    }


}
