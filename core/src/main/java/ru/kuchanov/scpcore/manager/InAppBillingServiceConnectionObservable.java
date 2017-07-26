package ru.kuchanov.scpcore.manager;

import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class InAppBillingServiceConnectionObservable {
    private static InAppBillingServiceConnectionObservable ourInstance = new InAppBillingServiceConnectionObservable();

    public static InAppBillingServiceConnectionObservable getInstance() {
        if (ourInstance == null) {
            ourInstance = new InAppBillingServiceConnectionObservable();
        }
        return ourInstance;
    }

    private Subject<Boolean, Boolean> accessDeniedObservable;

    private InAppBillingServiceConnectionObservable() {
        accessDeniedObservable = PublishSubject.create();
    }

    public Subject<Boolean, Boolean> getServiceStatusObservable() {
        return accessDeniedObservable;
    }
}