import * as Const from '../constants/Constants'
import { invokeActivity } from './InvokeActivity';
import {SERVER_PORT} from "../constants/Constants";

export async function createInstance(instance, userId, invoke_type, category = 'general') {
	instance['category'] = category;

	const request_body = JSON.stringify({
		type: 'article',
		name: instance.url,
		active: true,
		createdBy: {
			userId: {
				domain: Const.DOMAIN,
				email: Const.MANAGER_MAIL
			}
		},
		instanceAttributes: {
			theArticle: instance
		}
	});

	await fetch('http://localhost:' + SERVER_PORT + '/iob/instances', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json',
			credentials: 'include',
		},
		body: request_body
	}).then((response) => {
		if (response.status === 200) {
			response.json().then((res) => {
				invokeActivity(res.instanceId, userId, invoke_type);
			}, (error) => {
				console.log(error);
			});
		}
	});
}