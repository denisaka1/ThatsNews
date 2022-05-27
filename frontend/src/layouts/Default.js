import React, {useEffect, useState} from 'react';
import CarouselComp from '../components/Carousels/Carousels';
import Blocks from '../components/Blocks/Blocks';
import * as Const from '../constants/Constants'
import {v1} from 'uuid';

function Default(props) {
    const [articles, setArticles] = useState();
    const [carouselItems, setCarouselItems] = useState();
    const apiCallURL = Const.NEWS_API_URL +
        'apiKey=' + Const.NEWS_API_KEY +
        '&country=' + Const.NEWS_API_COUNTRY +
        '&page=' + Const.NEWS_API_PAGE +
        '&pageSize=' + Const.NEWS_API_PAGE_SIZE +
        (props.category ? '&category=' + props.category : '');

    const getApiData = async () => {
        const response = await fetch(apiCallURL)
            .then((response) => response.json());

        let carousel = [];
        for (let i = 0; i < Const.CAROUSEL_ITEMS; i++) {
            carousel.push(response.articles[String(i)]);
            delete response.articles[String(i)];
        }

        setCarouselItems(carousel);
        setArticles(response.articles);
        console.log(response.articles);
    };

    useEffect(() => {
        getApiData();
    }, []);

    return (
        [<CarouselComp key={v1()} articles={carouselItems} category={props.category}/>,
            <Blocks key={v1()} articles={articles} category={props.category} favorites={[]} isFavPage={false}/>]
    );
}

export default Default;