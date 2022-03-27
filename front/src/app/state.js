import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

const state = {
    username: "-"
}

const mutations = {
    setuserName(state, name) {
        state.username = name;
    }

}




export default new Vuex.Store({
    state,
    mutations
})