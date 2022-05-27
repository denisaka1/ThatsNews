import {SAVE_USER} from '../constants/Constants'

const initState = {
    isLoggedIn: false,
    user: {
        userId: {
            domain: '2022b.elazar.fine',
            email: ''
        },
        role: 'player',
        username: '',
        avatar: ''
    }
};

const UserReducer = (state = initState, action) => {
    if (action.type === SAVE_USER) {
        return Object.assign({}, state, action.payload);
    }
    return state;
};

export default UserReducer;