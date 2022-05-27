// environment configure
// export const BASE_URL = 'http://localhost:3000';
export const DOMAIN = '2022b.elazar.fine';
export const MANAGER_MAIL = 'manager@dummy.com';
export const CAROUSEL_ITEMS = 4;
export const SERVER_PORT = 8081;

export const NEWS_API_KEY = '839040596d424b39a1eae0c8cd8b6d92';
export const NEWS_API_URL = 'https://newsapi.org/v2/top-headlines?';
export const NEWS_API_COUNTRY = 'us';
export const NEWS_API_PAGE = '0';
export const NEWS_API_PAGE_SIZE = '20';

export const READ_ARTICLE = 'READ_ARTICLE';
export const SAVE_FAVORITE = 'SAVE_FAVORITE';
export const REMOVE_FAVORITE = 'REMOVE_FAVORITE';
export const GET_FAVORITES = 'GET_FAVORITES';

// messages
export const ERROR_MSG = 'ERROR: ';
export const INCORRECT_FIELDS = 'One or more fields are incorrect';
export const EMPTY_FIELDS = 'Please fill the fields';

// actions types
export const SAVE_USER = 'SAVE_USER';

// labels
export const SIGNUP = 'Sign Up'
export const SIGNIN = 'Sign In'
export const EMAIL_ADDRESS_LABEL = 'E-Mail address';
export const USERNAME_LABEL = 'Username';
export const EMAIL_SAMPLE = 'name@example.com'
export const SUBMIT = 'Submit'

// functions
export const getRequestBody = (_instanceId, _userId) => {
    return JSON.stringify({
        type: GET_FAVORITES,
        instance: {
            instanceId: {
                domain: DOMAIN,
                id: _instanceId
            }
        },
        invokedBy: {
            userId: _userId
        }
    });
}

export const getFavoritesForUser = async (_user, _callback) => {
    const user = _user
    const instanceURL = 'http://localhost:' + SERVER_PORT + '/iob/instances/search/byType/fictiveInstance?userDomain='
        + DOMAIN + '&userEmail=' + user.userId.email;
    const activityURL = 'http://localhost:' + SERVER_PORT + '/iob/activities';

    const response = await fetch(instanceURL)
        .then((response) => response.json());
    const instanceId = response[0].instanceId.id;

    await fetch(activityURL, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            credentials: 'include',
        },
        body: getRequestBody(instanceId, user.userId)
    }).then((activityRes) => {
        if (activityRes.status === 200) {
            activityRes.json().then((data) => {
                let articles = data.map(instance => instance.instanceAttributes.theArticle)
                _callback(articles)
            });

        }
    }, (error) => {
        console.log(error);
    });
}