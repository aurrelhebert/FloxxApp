const HtmlWebpackPlugin = require('html-webpack-plugin'); // Require  html-webpack-plugin plugin
const VueLoaderPlugin = require('vue-loader/lib/plugin');
const webpack = require('webpack');


function mockApi(app) {

    app.post('/login', function(req, res) {
        res.json({ 'name': 'Fabrice Szanjderman', 'isAdmin': true, token: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiJhaGVyaXRpZXIiLCJmaXJzdG5hbWUiOiJhaGVyaXRpZXJAZ21haWwuY29tIiwiaXNBZG1pbiI6dHJ1ZX0.-ymZ5w8e6Whw2BYl0TVlqNA2q4mLe1YoEyjQsDxWJm0' });
    })

    app.get('/api/planning', function(req, res) {
        res.json([{
            "day": "friday",
            "rooms": [{
                "Amphi bleu": [{
                    "slotId": {
                        "id": "friday_b_amphi_13:30-14:15"
                    },
                    "roomId": "Maillot",
                    "fromTime": "09:30",
                    "toTime": "12:30",
                    "talk": {
                        "talkType": "University",
                        "title": "La révolution (wasm) est incroyable parce que vraie"
                    },
                    "day": "wednesday"
                }, {
                    "slotId": {
                        "id": "friday_b_amphi_13:30-14:15"
                    },
                    "roomId": "Maillot",
                    "fromTime": "09:30",
                    "toTime": "12:30",
                    "talk": {
                        "talkType": "University",
                        "title": "La révolution (wasm) est incroyable parce que vraie"
                    },
                    "day": "wednesday"
                }]

            }]
        }, {
            "day": "thursday",
            "rooms": [{
                "Maillot": [{
                    "slotId": {
                        "id": "friday_b_amphi_13:30-14:15"
                    },
                    "roomId": "Maillot",
                    "fromTime": "09:30",
                    "toTime": "12:30",
                    "talk": {
                        "talkType": "University",
                        "title": "La révolution (wasm) est incroyable parce que vraie"
                    },
                    "day": "wednesday"
                }, {
                    "slotId": {
                        "id": "friday_b_amphi_13:30-14:15"
                    },
                    "roomId": "Maillot",
                    "fromTime": "09:30",
                    "toTime": "12:30",
                    "talk": {
                        "talkType": "University",
                        "title": "La révolution (wasm) est incroyable parce que vraie"
                    },
                    "day": "wednesday"
                }]
            }]
        }, {
            "day": "wednesday",
            "rooms": [{
                "Maillot": [{
                    "slotId": {
                        "id": "friday_b_amphi_13:30-14:15"
                    },
                    "roomId": "Maillot",
                    "fromTime": "09:30",
                    "toTime": "12:30",
                    "talk": {
                        "talkType": "University",
                        "title": "La révolution (wasm) est incroyable parce que vraie"
                    },
                    "day": "wednesday"
                }, {
                    "slotId": {
                        "id": "friday_b_amphi_13:30-14:15"
                    },
                    "roomId": "Maillot",
                    "fromTime": "09:30",
                    "toTime": "12:30",
                    "talk": {
                        "talkType": "University",
                        "title": "La révolution (wasm) est incroyable parce que vraie"
                    },
                    "day": "wednesday"
                }]
            }]

        }])

    })
}


module.exports = env => {
    return {
        entry: __dirname + "/src/app/desktop.js", // webpack entry point. Module to start building dependency graph
        output: {
            path: __dirname + '/dist/desktop', // Folder to store generated bundle
            filename: 'desktop.js', // Name of generated bundle after build
            publicPath: '/' // public URL of the output directory when referenced in a browser
        },
        plugins: [
            new HtmlWebpackPlugin({
                template: __dirname + "/src/public/desktop/index.html",
                inject: 'body'
            }),
            new VueLoaderPlugin(),

        ],
        devServer: {
            contentBase: './src/public/desktop',
            port: 8083,
            host: '0.0.0.0',
            disableHostCheck: true,
            before: function(app) {
                mockApi(app)
            }
        },
        resolve: {
            alias: {
                'vue': 'vue/dist/vue.esm.js'
            }
        },
        module: {
            rules: [{
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            }, {
                test: /\.vue$/,
                loader: 'vue-loader'
            }]
        }
    }
}