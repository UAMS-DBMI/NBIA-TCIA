import { Injectable } from '@angular/core';

@Injectable()
export class InitMonitorService{

    collectionsInit = false;
    modalityInit = false;
    anatomicalSiteInit = false;
    speciesInit = false;
    minimumStudiesInit = false;
    dateRangeInit = false;

    collectionsRunning = false;
    modalityRunning = false;
    anatomicalSiteRunning = false;
    speciesRunning = false;
    minimumStudiesRunning = false;
    dateRangeRunning = false;

    constructor() {
    }

    setDateRangeInit( status ) {
        this.dateRangeInit = status;
    }

    getDateRangeInit() {
        return this.dateRangeInit;
    }

    setCollectionsInit( status ) {
        this.collectionsInit = status;
    }

    getCollectionsInit() {
        return this.collectionsInit;
    }

    setModalityInit( status ) {
        this.modalityInit = status;
    }

    getModalityInit() {
        return this.modalityInit;
    }


    setAnatomicalSiteInit( status ) {
        this.anatomicalSiteInit = status;
    }

    getAnatomicalSiteInit() {
        return this.anatomicalSiteInit;
    }

    setSpeciesInit(status) {
        this.speciesInit = status;
    }

    getSpeciesInit(){
        return this.speciesInit;
    }

    setMinimumStudiesInit( status ) {
        this.minimumStudiesInit = status;
    }

    getMinimumStudiesInit() {
        return this.minimumStudiesInit;
    }

    getAnyInit() {
        let res = false;
        if( this.collectionsInit ){
            res =  true;
        }
        if( this.modalityInit ){
            res =  true;
        }
        if( this.anatomicalSiteInit ){
            res =  true;
        }
        if( this.speciesInit ){
            res =  true;
        }
        if( this.minimumStudiesInit ){
            res =  true;
        }
        if( this.dateRangeInit ){
            res =  true;
        }
    return res;
    }


    setDateRangeRunning( status ) {
        this.dateRangeRunning = status;
    }

    getDateRangeRunning() {
        return this.dateRangeRunning;
    }

    setCollectionsRunning( status ) {
        this.collectionsRunning = status;
    }

    getCollectionsRunning() {
        return this.collectionsRunning;
    }

    setModalityRunning( status ) {
        this.modalityRunning = status;
    }

    getModalityRunning() {
        return this.modalityRunning;
    }


    setAnatomicalSiteRunning( status ) {
        this.anatomicalSiteRunning = status;
    }

    getAnatomicalSiteRunning() {
        return this.anatomicalSiteRunning;
    }

    setSpeciesRunning( status){
        this.speciesRunning = status;
    }

    get getSpeciesRunning(){
        return this.speciesRunning;
    }

    setMinimumStudiesRunning( status ) {
        this.minimumStudiesRunning = status;
    }

    getMinimumStudiesRunning() {
        return this.minimumStudiesRunning;
    }

    getAnyRunning() {
        let res = false;
        if( this.collectionsRunning ){
            res =  true;
        }
        if( this.modalityRunning ){
            res =  true;
        }
        if( this.anatomicalSiteRunning ){
            res =  true;
        }
         if( this.speciesRunning ){
            res =  true;
        }
        if( this.minimumStudiesRunning ){
            res =  true;
        }
        if( this.dateRangeRunning ){
            res =  true;
        }
    return res;
    }
}
