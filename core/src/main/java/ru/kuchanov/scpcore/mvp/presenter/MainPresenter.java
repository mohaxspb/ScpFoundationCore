package ru.kuchanov.scpcore.mvp.presenter;


import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.base.BaseDrawerPresenter;
import ru.kuchanov.scpcore.mvp.contract.MainMvp;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for TappAwards
 */
public class MainPresenter
        extends BaseDrawerPresenter<MainMvp.View>
        implements MainMvp.Presenter {

    public MainPresenter(MyPreferenceManager myPreferencesManager, DbProviderFactory dbProviderFactory, ApiClient apiClient) {
        super(myPreferencesManager, dbProviderFactory, apiClient);
    }
}