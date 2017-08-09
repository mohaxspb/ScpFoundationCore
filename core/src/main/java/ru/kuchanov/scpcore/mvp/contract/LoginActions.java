package ru.kuchanov.scpcore.mvp.contract;


import com.afollestad.materialdialogs.MaterialDialog;

import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.db.model.User;

/**
 * Created by mohax on 24.03.2017.
 * <p>
 * for scp_ru
 */
public interface LoginActions {

    interface View {

        void startLogin(Constants.Firebase.SocialProvider provider);

        void updateUser(User user);

        void showLoginProvidersPopup();

        void showOfferLoginPopup(MaterialDialog.SingleButtonCallback cancelCallbak);
    }

    interface Presenter {

        void startFirebaseLogin(Constants.Firebase.SocialProvider provider, String id);

        void logoutUser();
    }
}