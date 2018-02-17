package ru.kuchanov.scpcore.manager;

import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public final class InAppBillingServiceConnectionObservable {

    private static InAppBillingServiceConnectionObservable ourInstance = new InAppBillingServiceConnectionObservable();

    public static InAppBillingServiceConnectionObservable getInstance() {
        if (ourInstance == null) {
            ourInstance = new InAppBillingServiceConnectionObservable();
        }
        return ourInstance;
    }

    private final Subject<Boolean, Boolean> accessDeniedObservable;

    private InAppBillingServiceConnectionObservable() {
        super();
        accessDeniedObservable = PublishSubject.create();
    }

    public Subject<Boolean, Boolean> getServiceStatusObservable() {
        return accessDeniedObservable;
    }
}