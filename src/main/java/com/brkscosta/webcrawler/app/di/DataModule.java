package com.brkscosta.webcrawler.app.di;

import com.brkscosta.webcrawler.data.repositories.CareTakerRepository;
import com.brkscosta.webcrawler.data.repositories.Originator;
import com.brkscosta.webcrawler.data.repositories.CrawlerRepository;
import com.brkscosta.webcrawler.data.repositories.WebPageRepository;
import com.google.inject.AbstractModule;

public class DataModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(WebPageRepository.class).asEagerSingleton();
        bind(CareTakerRepository.class).asEagerSingleton();
        bind(Originator.class).to(CrawlerRepository.class).asEagerSingleton();
    }
}
