package ru.kuchanov.scpcore.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import io.realm.RealmList;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.ParseHtmlUtils;
import ru.kuchanov.scpcore.controller.adapter.viewmodel.MyListItem;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.db.model.RealmString;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.model.ScpArtAdsJson;
import ru.kuchanov.scpcore.ui.holder.article.ArticleImageHolder;
import ru.kuchanov.scpcore.ui.holder.article.ArticleSpoilerHolder;
import ru.kuchanov.scpcore.ui.holder.article.ArticleTableHolder;
import ru.kuchanov.scpcore.ui.holder.article.ArticleTabsHolder;
import ru.kuchanov.scpcore.ui.holder.article.ArticleTagsHolder;
import ru.kuchanov.scpcore.ui.holder.article.ArticleTextHolder;
import ru.kuchanov.scpcore.ui.holder.article.ArticleTitleHolder;
import ru.kuchanov.scpcore.ui.holder.article.NativeAdsArticleListHolder;
import ru.kuchanov.scpcore.ui.model.ArticleTextPartViewModel;
import ru.kuchanov.scpcore.ui.model.SpoilerViewModel;
import ru.kuchanov.scpcore.ui.model.TabsViewModel;
import ru.kuchanov.scpcore.ui.util.SetTextViewHTML;
import timber.log.Timber;

import static ru.kuchanov.scpcore.Constants.Firebase.RemoteConfigKeys.NATIVE_ADS_LISTS_INTERVAL;
import static ru.kuchanov.scpcore.ui.adapter.ArticlesListAdapter.createAdsModelsList;

/**
 * Created by Dante on 17.01.2016.
 * <p>
 * for scp_ru
 */
public class ArticleAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements ArticleSpoilerHolder.SpoilerClickListener, ArticleTabsHolder.MyTabClickListener {

    //TODO realize via enum
    private static final int TYPE_TEXT = 0;

    private static final int TYPE_SPOILER = 1;

    private static final int TYPE_IMAGE = 2;

    private static final int TYPE_TITLE = 3;

    private static final int TYPE_TABLE = 4;

    private static final int TYPE_TAGS = 5;

    private static final int TYPE_TABS = 6;

    private static final int TYPE_NATIVE_APPODEAL = 7;

    private static final int TYPE_NATIVE_SCP_ART = 8;

    private static final int TYPE_NATIVE_SCP_QUIZ = 9;

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    private final List<MyListItem> mAdsModelsList = new ArrayList<>();

    private final List<MyListItem> mViewModels = new ArrayList<>();

    private List<TabsViewModel> mTabsViewModelList = new ArrayList<>();

    private List<SpoilerViewModel> mExpandedSpoilers = new ArrayList<>();

    @Inject
    Gson mGson;

    public ArticleAdapter() {
        super();
        BaseApplication.getAppComponent().inject(this);
    }

    public List<String> getArticlesTextParts() {
        return ArticleTextPartViewModel.convertToStringList(mViewModels);
    }

    private SetTextViewHTML.TextItemsClickListener mTextItemsClickListener;

    public void setTextItemsClickListener(final SetTextViewHTML.TextItemsClickListener textItemsClickListener) {
        mTextItemsClickListener = textItemsClickListener;
    }

    public void setData(
            final Article article,
            final List<SpoilerViewModel> expandedSpoilers,
            final List<TabsViewModel> tabsViewModels
    ) {
        mTabsViewModelList = tabsViewModels;
        mExpandedSpoilers = expandedSpoilers;

        mViewModels.clear();

        final List<String> articlesTextParts = new ArrayList<>(RealmString.toStringList(article.textParts));
        final @ParseHtmlUtils.TextType List<String> articlesTextPartsTypes = new ArrayList<>(RealmString.toStringList(article.textPartsTypes));

        articlesTextParts.add(0, article.title);
        articlesTextPartsTypes.add(0, ParseHtmlUtils.TextType.TITLE);
        //DO NOT USE THIS VALUE!!!
        articlesTextParts.add(article.tags.toString());
        articlesTextPartsTypes.add(ParseHtmlUtils.TextType.TAGS);

        for (int order = 0; order < articlesTextParts.size(); order++) {
            @ParseHtmlUtils.TextType final String type = articlesTextPartsTypes.get(order);
            switch (type) {
                case ParseHtmlUtils.TextType.SPOILER: {
                    final List<String> spoilerParts = ParseHtmlUtils.parseSpoilerParts(articlesTextParts.get(order));

                    final SpoilerViewModel spoilerViewModel = new SpoilerViewModel();
                    spoilerViewModel.titles = new ArrayList<>(spoilerParts.subList(0, 2));
                    spoilerViewModel.mSpoilerTextParts = ParseHtmlUtils.getArticlesTextParts(spoilerParts.get(2));
                    spoilerViewModel.mSpoilerTextPartsTypes = ParseHtmlUtils.getListOfTextTypes(spoilerViewModel.mSpoilerTextParts);
                    spoilerViewModel.isExpanded = expandedSpoilers.contains(spoilerViewModel);

                    mViewModels.add(new ArticleTextPartViewModel(type, spoilerViewModel, false));
                    //add textParts for expanded spoilers
                    if (spoilerViewModel.isExpanded) {
                        Timber.d("expanded spoiler title: %s", spoilerViewModel.titles.get(0));
                        final Collection<ArticleTextPartViewModel> viewModels = new ArrayList<>();
                        for (int i = 0; i < spoilerViewModel.mSpoilerTextPartsTypes.size(); i++) {
                            @ParseHtmlUtils.TextType final String typeInSpoiler = spoilerViewModel.mSpoilerTextPartsTypes.get(i);

                            Timber.d("expanded spoiler type: %s", typeInSpoiler);

                            //handle tabs
                            if (typeInSpoiler.equals(ParseHtmlUtils.TextType.TABS)) {
                                final TabsViewModel tabsViewModel = ParseHtmlUtils.parseTabs(spoilerViewModel.mSpoilerTextParts.get(i));

                                tabsViewModel.isInSpoiler = true;

                                //get and set state (index of opened tab)
                                if (mTabsViewModelList.contains(tabsViewModel)) {
                                    final TabsViewModel savedOne = mTabsViewModelList.get(mTabsViewModelList.indexOf(tabsViewModel));
                                    Timber.d("savedOne selected tab: %s", savedOne.getCurrentTab());
                                    tabsViewModel.setCurrentTab(savedOne.getCurrentTab());
                                } else {
                                    Timber.d("mTabsViewModelList.size: %s", mTabsViewModelList.size());
                                    for (final TabsViewModel tabsViewModel1 : mTabsViewModelList) {
                                        Timber.d("selected tab: %s", tabsViewModel1.getCurrentTab());
                                    }
                                }

                                //add textParts for expanded spoilers
                                final Collection<ArticleTextPartViewModel> viewModelsTabs = new ArrayList<>();
                                for (int u = 0; u < tabsViewModel.getTabDataList().get(tabsViewModel.getCurrentTab()).getTextParts().size(); u++) {
                                    final TabsViewModel.TabData tabData = tabsViewModel.getTabDataList().get(tabsViewModel.getCurrentTab());
                                    @ParseHtmlUtils.TextType final String typeInTab = tabData.getTextPartsTypes().get(u);
                                    viewModelsTabs.add(new ArticleTextPartViewModel(typeInTab, tabData.getTextParts().get(u), true));
                                }
                                viewModels.add(new ArticleTextPartViewModel(typeInSpoiler, tabsViewModel, true));
                                viewModels.addAll(viewModelsTabs);
                            } else {
                                final String dataInSpoiler = spoilerViewModel.mSpoilerTextParts.get(i);
                                viewModels.add(new ArticleTextPartViewModel(typeInSpoiler, dataInSpoiler, true));
                            }
                        }
                        mViewModels.addAll(viewModels);
                    }
                    break;
                }
                case ParseHtmlUtils.TextType.TABS: {
                    //create and set ViewModel (as for spoiler)
                    final TabsViewModel tabsViewModel = ParseHtmlUtils.parseTabs(articlesTextParts.get(order));

                    //get and set state (index of opened tab)
                    if (mTabsViewModelList.contains(tabsViewModel)) {
                        final TabsViewModel savedOne = mTabsViewModelList.get(mTabsViewModelList.indexOf(tabsViewModel));
                        tabsViewModel.setCurrentTab(savedOne.getCurrentTab());
                    }

                    //add textParts for expanded spoilers
                    final Collection<ArticleTextPartViewModel> viewModels = new ArrayList<>();
                    for (int i = 0; i < tabsViewModel.getTabDataList().get(tabsViewModel.getCurrentTab()).getTextParts().size(); i++) {
                        final TabsViewModel.TabData tabData = tabsViewModel.getTabDataList().get(tabsViewModel.getCurrentTab());
                        @ParseHtmlUtils.TextType final String typeInTab = tabData.getTextPartsTypes().get(i);
                        //spoiler support
                        Object dataInTab = tabData.getTextParts().get(i);
                        final boolean isSpoiler = typeInTab.equals(ParseHtmlUtils.TextType.SPOILER);
                        if (isSpoiler) {
                            final List<String> spoilerData = ParseHtmlUtils.parseSpoilerParts((String) dataInTab);

                            final SpoilerViewModel spoilerViewModel = new SpoilerViewModel();
                            spoilerViewModel.titles = new ArrayList<>(spoilerData.subList(0, 2));
                            spoilerViewModel.mSpoilerTextParts = ParseHtmlUtils.getArticlesTextParts(spoilerData.get(2));
                            spoilerViewModel.mSpoilerTextPartsTypes = ParseHtmlUtils.getListOfTextTypes(spoilerViewModel.mSpoilerTextParts);
                            spoilerViewModel.isExpanded = expandedSpoilers.contains(spoilerViewModel);

                            dataInTab = spoilerViewModel;
                            viewModels.add(new ArticleTextPartViewModel(typeInTab, dataInTab, true));

                            //add textParts for expanded spoilers
                            if (spoilerViewModel.isExpanded) {
                                Timber.d("expanded spoiler title: %s", spoilerViewModel.titles.get(0));
                                final List<ArticleTextPartViewModel> viewModelsInSpoiler = new ArrayList<>();
                                for (int u = 0; u < spoilerViewModel.mSpoilerTextPartsTypes.size(); u++) {
                                    @ParseHtmlUtils.TextType final String typeInSpoiler = spoilerViewModel.mSpoilerTextPartsTypes.get(u);

                                    Timber.d("expanded spoiler type: %s", typeInSpoiler);

                                    final String dataInSpoiler = spoilerViewModel.mSpoilerTextParts.get(u);
                                    viewModelsInSpoiler.add(new ArticleTextPartViewModel(typeInSpoiler, dataInSpoiler, true));

                                }
                                viewModels.addAll(viewModelsInSpoiler);
                            }
                        } else {
                            viewModels.add(new ArticleTextPartViewModel(typeInTab, dataInTab, false));
                        }
                    }
                    mViewModels.add(new ArticleTextPartViewModel(type, tabsViewModel, false));
                    mViewModels.addAll(viewModels);
                    break;
                }
                case ParseHtmlUtils.TextType.TAGS:
                    mViewModels.add(new ArticleTextPartViewModel(type, article.tags, false));
                    break;
                default:
                    mViewModels.add(new ArticleTextPartViewModel(type, articlesTextParts.get(order), false));
                    break;
            }
        }

        addAds();

        //log
        @ParseHtmlUtils.TextType final List<String> types = new ArrayList<>();
        final List<Boolean> isInSpoilerList = new ArrayList<>();
        for (final MyListItem model : mViewModels) {
            types.add(((ArticleTextPartViewModel) model).type);
            isInSpoilerList.add(((ArticleTextPartViewModel) model).isInSpoiler);
        }

        notifyDataSetChanged();
    }

    private void addAds() {
        //do not add native ads items if user has subscription or banners temporary disabled
        //or banners enabled or native disabled
        final FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        if (mMyPreferenceManager.isHasAnySubscription()
                || mMyPreferenceManager.isTimeToShowBannerAds()
                || mMyPreferenceManager.isBannerInArticleEnabled()) {
            return;
        }
        if (mAdsModelsList.isEmpty()) {
            mAdsModelsList.addAll(createAdsModelsList(true, mMyPreferenceManager));
        }

        // Loop through the items array and place a new Native Express ad in every ith position in
        // the items List.
        final int interval = (int) (config.getLong(NATIVE_ADS_LISTS_INTERVAL) - 1);
        for (int i = 0; i <= mViewModels.size(); i += interval) {
            //do not add as first row
            if (i == 0) {
                continue;
            } else if (i / interval > Constants.NUM_OF_NATIVE_ADS_PER_SCREEN) {
                break;
            }

            mViewModels.add(i, mAdsModelsList.get((i / interval) - 1));
        }
    }

    @Override
    public int getItemViewType(final int position) {
        @ParseHtmlUtils.TextType final String type = ((ArticleTextPartViewModel) mViewModels.get(position)).type;
        switch (type) {
            case ParseHtmlUtils.TextType.TITLE:
                return TYPE_TITLE;
            case ParseHtmlUtils.TextType.TEXT:
                return TYPE_TEXT;
            case ParseHtmlUtils.TextType.IMAGE:
                return TYPE_IMAGE;
            case ParseHtmlUtils.TextType.SPOILER:
                return TYPE_SPOILER;
            case ParseHtmlUtils.TextType.TABLE:
                return TYPE_TABLE;
            case ParseHtmlUtils.TextType.TAGS:
                return TYPE_TAGS;
            case ParseHtmlUtils.TextType.TABS:
                return TYPE_TABS;
            case ParseHtmlUtils.TextType.NATIVE_ADS_SCP_ART:
                return TYPE_NATIVE_SCP_ART;
            case ParseHtmlUtils.TextType.NATIVE_ADS_APPODEAL:
                return TYPE_NATIVE_APPODEAL;
            case ParseHtmlUtils.TextType.NATIVE_ADS_SCP_QUIZ:
                return TYPE_NATIVE_SCP_QUIZ;
            default:
                throw new IllegalArgumentException("unexpected type: " + type);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View view;
        switch (viewType) {
            case TYPE_TITLE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_title, parent, false);
                return new ArticleTitleHolder(view);
            case TYPE_IMAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_r_img, parent, false);
                return new ArticleImageHolder(view, mTextItemsClickListener);
            case TYPE_TEXT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_text, parent, false);
                return new ArticleTextHolder(view, mTextItemsClickListener);
            case TYPE_SPOILER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_spoiler, parent, false);
                return new ArticleSpoilerHolder(view, this);
            case TYPE_TABLE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_table, parent, false);
                return new ArticleTableHolder(view, mTextItemsClickListener);
            case TYPE_TAGS:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_tags, parent, false);
                return new ArticleTagsHolder(view, mTextItemsClickListener);
            case TYPE_TABS:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_tabs, parent, false);
                return new ArticleTabsHolder(view, this);
            case TYPE_NATIVE_SCP_ART:
            case TYPE_NATIVE_APPODEAL:
            case TYPE_NATIVE_SCP_QUIZ:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_article_native_container, parent, false);
                return new NativeAdsArticleListHolder(view, mTextItemsClickListener);
            default:
                throw new IllegalArgumentException("unexpected type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final ArticleTextPartViewModel textPartViewModel = (ArticleTextPartViewModel) mViewModels.get(position);
        switch (getItemViewType(position)) {
            case TYPE_TITLE:
                ((ArticleTitleHolder) holder).bind(textPartViewModel);
                break;
            case TYPE_TEXT:
                ((ArticleTextHolder) holder).bind(textPartViewModel);
                break;
            case TYPE_IMAGE:
                ((ArticleImageHolder) holder).bind(textPartViewModel);
                break;
            case TYPE_SPOILER:
                ((ArticleSpoilerHolder) holder).bind((SpoilerViewModel) textPartViewModel.data);
                break;
            case TYPE_TABLE:
                ((ArticleTableHolder) holder).bind(textPartViewModel);
                break;
            case TYPE_TAGS:
                ((ArticleTagsHolder) holder).bind((RealmList<ArticleTag>) textPartViewModel.data);
                break;
            case TYPE_TABS:
                ((ArticleTabsHolder) holder).bind((TabsViewModel) textPartViewModel.data);
                break;
            case TYPE_NATIVE_APPODEAL: {
                ((NativeAdsArticleListHolder) holder).bind((Integer) textPartViewModel.data);
            }
            break;
            case TYPE_NATIVE_SCP_ART:
                ((NativeAdsArticleListHolder) holder).bind((ScpArtAdsJson.ScpArtAd) textPartViewModel.data);
                break;
            case TYPE_NATIVE_SCP_QUIZ:
                ((NativeAdsArticleListHolder) holder).bind();
                break;
            default:
                throw new IllegalArgumentException("unexpected item type: " + getItemViewType(position));
        }
    }

    @Override
    public int getItemCount() {
        return mViewModels.size();
    }

    @Override
    public long getItemId(final int position) {
        return ((ArticleTextPartViewModel) mViewModels.get(position)).data.hashCode();
    }

    @Override
    public void onSpoilerExpand(final int position) {
        Timber.d("onSpoilerExpand: %s", position);
        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        final SpoilerViewModel spoilerViewModel = ((SpoilerViewModel) ((ArticleTextPartViewModel) mViewModels.get(position)).data);
//        Timber.d("mSpoilerTextPartsTypes size: %s", spoilerViewModel.mSpoilerTextPartsTypes.size());
//        Timber.d("mSpoilerTextPartsTypes: %s", spoilerViewModel.mSpoilerTextPartsTypes);
//        Timber.d("mSpoilerTextParts size: %s", spoilerViewModel.mSpoilerTextParts.size());
//        Timber.d("mSpoilerTextParts: %s", spoilerViewModel.mSpoilerTextParts);
        final Collection<ArticleTextPartViewModel> viewModels = new ArrayList<>();
        for (int order = 0; order < spoilerViewModel.mSpoilerTextPartsTypes.size(); order++) {
            @ParseHtmlUtils.TextType final String type = spoilerViewModel.mSpoilerTextPartsTypes.get(order);
            if (type.equals(ParseHtmlUtils.TextType.TABS)) {
                final TabsViewModel tabsViewModel = ParseHtmlUtils.parseTabs(spoilerViewModel.mSpoilerTextParts.get(order));

                tabsViewModel.isInSpoiler = true;

                //get and set state (index of opened tab)
                if (mTabsViewModelList.contains(tabsViewModel)) {
                    final TabsViewModel savedOne = mTabsViewModelList.get(mTabsViewModelList.indexOf(tabsViewModel));
                    Timber.d("savedOne selected tab: %s", savedOne.getCurrentTab());
                    tabsViewModel.setCurrentTab(savedOne.getCurrentTab());
                } else {
                    Timber.d("mTabsViewModelList.size: %s", mTabsViewModelList.size());
                    for (final TabsViewModel tabsViewModel1 : mTabsViewModelList) {
                        Timber.d("selected tab: %s", tabsViewModel1.getCurrentTab());
                    }
                }

                viewModels.add(new ArticleTextPartViewModel(type, tabsViewModel, true));
                //add textParts for expanded spoilers
                final Collection<ArticleTextPartViewModel> viewModelsTabs = new ArrayList<>();
                for (int i = 0; i < tabsViewModel.getTabDataList().get(tabsViewModel.getCurrentTab()).getTextParts().size(); i++) {
                    final TabsViewModel.TabData tabData = tabsViewModel.getTabDataList().get(tabsViewModel.getCurrentTab());
                    @ParseHtmlUtils.TextType final String typeInSpoiler = tabData.getTextPartsTypes().get(i);
                    final String dataInSpoiler = tabData.getTextParts().get(i);
                    viewModelsTabs.add(new ArticleTextPartViewModel(typeInSpoiler, dataInSpoiler, true));
                }
                viewModels.addAll(viewModelsTabs);
            } else {
                viewModels.add(new ArticleTextPartViewModel(type, spoilerViewModel.mSpoilerTextParts.get(order), true));
            }
        }
        mViewModels.addAll(position + 1, viewModels);

        notifyItemRangeInserted(position + 1, viewModels.size());

        mTextItemsClickListener.onSpoilerExpand(spoilerViewModel);
    }

    @Override
    public void onSpoilerCollapse(final int position) {
        Timber.d("onSpoilerCollapse: %s", position);
        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        final SpoilerViewModel spoilerViewModel = ((SpoilerViewModel) ((ArticleTextPartViewModel) mViewModels.get(position)).data);
        final Collection<ArticleTextPartViewModel> viewModels = new ArrayList<>();
        for (int order = 0; order < spoilerViewModel.mSpoilerTextPartsTypes.size(); order++) {
            @ParseHtmlUtils.TextType final String type = spoilerViewModel.mSpoilerTextPartsTypes.get(order);

            if (type.equals(ParseHtmlUtils.TextType.TABS)) {
                final Iterable<MyListItem> subList = new ArrayList<>(mViewModels.subList(position, mViewModels.size()));
                TabsViewModel tabsViewModel = null;
                for (final MyListItem partViewModel : subList) {
                    if (((ArticleTextPartViewModel) partViewModel).type.equals(ParseHtmlUtils.TextType.TABS)) {
                        tabsViewModel = (TabsViewModel) ((ArticleTextPartViewModel) partViewModel).data;
                    }
                }
                if (tabsViewModel == null) {
                    throw new IllegalStateException("tabsViewModel is null while collapse spoiler!");
                }

                viewModels.add(new ArticleTextPartViewModel(type, tabsViewModel, false));

                for (final TabsViewModel.TabData tabData : tabsViewModel.getTabDataList()) {
                    final Collection<ArticleTextPartViewModel> viewModelsTabs = new ArrayList<>();
                    for (int i = 0; i < tabData.getTextParts().size(); i++) {
                        @ParseHtmlUtils.TextType final String typeInSpoiler = tabData.getTextPartsTypes().get(i);
                        final String dataInSpoiler = tabData.getTextParts().get(i);
                        viewModelsTabs.add(new ArticleTextPartViewModel(typeInSpoiler, dataInSpoiler, true));
                    }
                    if (mViewModels.containsAll(viewModelsTabs)) {
                        viewModels.addAll(viewModelsTabs);
                    }
                }
            } else {
                final String data = spoilerViewModel.mSpoilerTextParts.get(order);
                viewModels.add(new ArticleTextPartViewModel(type, data, true));
            }
        }

        mViewModels
                .subList(position + 1, position + 1 + viewModels.size())
                .clear();

        notifyItemRangeRemoved(position + 1, viewModels.size());

        mTextItemsClickListener.onSpoilerCollapse(spoilerViewModel);
    }

    @Override
    public void onTabSelected(final int positionInList, final int positionInTabs) {
        Timber.d("onTabSelected: %s/%s", positionInList, positionInTabs);

        if (positionInList == RecyclerView.NO_POSITION) {
            return;
        }

        final TabsViewModel tabsViewModel = ((TabsViewModel) ((ArticleTextPartViewModel) mViewModels.get(positionInList)).data);
        tabsViewModel.setCurrentTab(positionInTabs);

        for (final TabsViewModel.TabData tabData : tabsViewModel.getTabDataList()) {
            final Collection<ArticleTextPartViewModel> viewModelsTabs = new ArrayList<>();

            for (int i = 0; i < tabData.getTextParts().size(); i++) {
                @ParseHtmlUtils.TextType final String typeInTab = tabData.getTextPartsTypes().get(i);
                Object dataInTab = tabData.getTextParts().get(i);

                final boolean isSpoiler = typeInTab.equals(ParseHtmlUtils.TextType.SPOILER);
                if (isSpoiler) {
                    final List<String> spoilerData = ParseHtmlUtils.parseSpoilerParts((String) dataInTab);

                    final SpoilerViewModel spoilerViewModel = new SpoilerViewModel();
                    spoilerViewModel.titles = new ArrayList<>(spoilerData.subList(0, 2));
                    spoilerViewModel.mSpoilerTextParts = ParseHtmlUtils.getArticlesTextParts(spoilerData.get(2));
                    spoilerViewModel.mSpoilerTextPartsTypes = ParseHtmlUtils.getListOfTextTypes(spoilerViewModel.mSpoilerTextParts);
                    spoilerViewModel.isExpanded = mExpandedSpoilers.contains(spoilerViewModel);

                    dataInTab = spoilerViewModel;
                    viewModelsTabs.add(new ArticleTextPartViewModel(typeInTab, dataInTab, true));

                    //add textParts for expanded spoilers
                    if (spoilerViewModel.isExpanded) {
                        Timber.d("expanded spoiler title: %s", spoilerViewModel.titles.get(0));
                        final List<ArticleTextPartViewModel> viewModelsInSpoiler = new ArrayList<>();
                        for (int u = 0; u < spoilerViewModel.mSpoilerTextPartsTypes.size(); u++) {
                            @ParseHtmlUtils.TextType final String typeInSpoiler = spoilerViewModel.mSpoilerTextPartsTypes.get(u);

                            Timber.d("expanded spoiler type: %s", typeInSpoiler);

                            final String dataInSpoiler = spoilerViewModel.mSpoilerTextParts.get(u);
                            viewModelsInSpoiler.add(new ArticleTextPartViewModel(typeInSpoiler, dataInSpoiler, true));

                        }
                        viewModelsTabs.addAll(viewModelsInSpoiler);
                    }
                } else {
                    viewModelsTabs.add(new ArticleTextPartViewModel(typeInTab, dataInTab, false));
                }
            }
            if (mViewModels.containsAll(viewModelsTabs)) {
                mViewModels
                        .subList(positionInList + 1, positionInList + 1 + viewModelsTabs.size())
                        .clear();

                notifyItemRangeRemoved(positionInList + 1, viewModelsTabs.size());
            }
        }

        //add new viewModels
        final Collection<ArticleTextPartViewModel> viewModelsTabs = new ArrayList<>();
        for (int i = 0; i < tabsViewModel.getTabDataList().get(positionInTabs).getTextParts().size(); i++) {
            final TabsViewModel.TabData tabData = tabsViewModel.getTabDataList().get(positionInTabs);
            @ParseHtmlUtils.TextType final String typeInTab = tabData.getTextPartsTypes().get(i);

            Object dataInTab = tabData.getTextParts().get(i);
            final boolean isSpoiler = typeInTab.equals(ParseHtmlUtils.TextType.SPOILER);
            if (isSpoiler) {
                final List<String> spoilerData = ParseHtmlUtils.parseSpoilerParts((String) dataInTab);

                final SpoilerViewModel spoilerViewModel = new SpoilerViewModel();
                spoilerViewModel.titles = new ArrayList<>(spoilerData.subList(0, 2));
                spoilerViewModel.mSpoilerTextParts = ParseHtmlUtils.getArticlesTextParts(spoilerData.get(2));
                spoilerViewModel.mSpoilerTextPartsTypes = ParseHtmlUtils.getListOfTextTypes(spoilerViewModel.mSpoilerTextParts);
                spoilerViewModel.isExpanded = mExpandedSpoilers.contains(spoilerViewModel);

                dataInTab = spoilerViewModel;
                viewModelsTabs.add(new ArticleTextPartViewModel(typeInTab, dataInTab, true));

                //add textParts for expanded spoilers
                if (spoilerViewModel.isExpanded) {
                    final List<ArticleTextPartViewModel> viewModelsInSpoiler = new ArrayList<>();
                    Timber.d("expanded spoiler title: %s", spoilerViewModel.titles.get(0));
                    for (int u = 0; u < spoilerViewModel.mSpoilerTextPartsTypes.size(); u++) {
                        @ParseHtmlUtils.TextType final String typeInSpoiler = spoilerViewModel.mSpoilerTextPartsTypes.get(u);

                        Timber.d("expanded spoiler type: %s", typeInSpoiler);

                        final String dataInSpoiler = spoilerViewModel.mSpoilerTextParts.get(u);
                        viewModelsInSpoiler.add(new ArticleTextPartViewModel(typeInSpoiler, dataInSpoiler, true));

                    }
                    viewModelsTabs.addAll(viewModelsInSpoiler);
                }
            } else {
                viewModelsTabs.add(new ArticleTextPartViewModel(typeInTab, dataInTab, false));
            }
        }
        final Collection<ArticleTextPartViewModel> viewModels = new ArrayList<>(viewModelsTabs);

        mViewModels.addAll(positionInList + 1, viewModels);

        notifyItemRangeInserted(positionInList + 1, viewModels.size());

        mTextItemsClickListener.onTabSelected(tabsViewModel);
    }
}