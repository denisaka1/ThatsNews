import { SAVE_USER } from '../constants/Constants';

export function saveUser(payload) {
	return {
		type: SAVE_USER, payload
	};
}