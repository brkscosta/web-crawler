package com.brkscosta.webcrawler.app.di;

import com.brkscosta.webcrawler.app.ui.main.MainView;
import com.brkscosta.webcrawler.app.ui.main.MainViewModel;
import com.brkscosta.webcrawler.app.utils.Logger;
import com.brkscosta.webcrawler.data.repositories.CareTakerRepository;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MainView.class).asEagerSingleton();
        bind(MainViewModel.class).asEagerSingleton();
        bind(CareTakerRepository.class).asEagerSingleton();
        bind(Logger.class).in(Singleton.class);
    }
}
